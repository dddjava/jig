package org.dddjava.jig.infrastructure.mybatis;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.dddjava.jig.domain.model.data.persistence.*;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisReadResult;
import org.dddjava.jig.domain.model.sources.mybatis.SqlReadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MyBatis用のSQL読み取り機
 *
 * 本クラスではMyBatisの内部APIを使用してSQLを取得しています。
 * ごくシンプルな使い方のみを想定しているため、高度な使い方をしている場合は正確な情報が取得できません。
 *
 * クラスパスにMapperが依存しているクラスが存在しない場合、MyBatisがMapperを読み取れないため解析できません。
 * それでも出力したい場合は、実行時に該当のクラスをクラスパスに含めてください。
 */
public class MyBatisStatementsReader {

    private static final Logger logger = LoggerFactory.getLogger(MyBatisStatementsReader.class);

    public MyBatisReadResult readFrom(Collection<JigTypeHeader> jigTypeHeaders, List<Path> classPaths) {
        // Mapperアノテーションがついているクラスを対象にする
        Collection<String> classNames = jigTypeHeaders.stream()
                .filter(jigTypeHeader -> jigTypeHeader.jigTypeAttributes()
                        .declaredAnnotation(TypeId.valueOf("org.apache.ibatis.annotations.Mapper")))
                .map(JigTypeHeader::fqn)
                .toList();

        // 該当なしの場合に余計なClassLoader生成やMyBatisの初期化を行わないための早期リターン
        if (classNames.isEmpty()) return new MyBatisReadResult(SqlStatements.empty(), SqlReadStatus.成功);

        URL[] classLocationUrls = classPaths.stream()
                .flatMap(path -> {
                    try {
                        return Stream.of(path.toUri().toURL());
                    } catch (MalformedURLException e) {
                        logger.warn("pathのURLへの変換に失敗しました。{}を読み飛ばします。", path, e);
                        return Stream.empty();
                    }
                })
                .toArray(URL[]::new);
        try (URLClassLoader classLoader = new URLClassLoader(classLocationUrls, Configuration.class.getClassLoader())) {
            Resources.setDefaultClassLoader(classLoader);

            return extractSql(classNames, classLoader);
        } catch (IOException e) {
            logger.warn("SQLファイルの読み込みでIO例外が発生しました。" +
                    "すべてのSQLは認識されません。リポジトリのCRUDは出力されませんが、他の出力には影響ありません。", e);
            return new MyBatisReadResult(SqlReadStatus.失敗);
        } catch (PersistenceException e) {
            logger.warn("SQL読み込み中にMyBatisに関する例外が発生しました。" +
                    "すべてのSQLは認識されません。リポジトリのCRUDは出力されませんが、他の出力には影響ありません。" +
                    "この例外は #228 #710 で確認していますが、情報が不足しています。発生条件をやスタックトレース等の情報をいただけると助かります。", e);
            return new MyBatisReadResult(SqlReadStatus.失敗);
        }
    }

    private MyBatisReadResult extractSql(Collection<String> classNames, ClassLoader classLoader) {
        SqlReadStatus sqlReadStatus = SqlReadStatus.成功;

        Configuration config = new Configuration();
        for (String className : classNames) {
            try {
                Class<?> mapperClass = classLoader.loadClass(className);
                config.addMapper(mapperClass);
            } catch (NoClassDefFoundError e) {
                logger.warn("{} がJIG実行時クラスパスに存在しないクラスに依存しているため読み取れませんでした。このMapperの読み取りはスキップします。" +
                                "メッセージ={}",
                        className, e.getLocalizedMessage());
                sqlReadStatus = SqlReadStatus.読み取り失敗あり;
            } catch (Exception e) {
                logger.warn("なんらかの例外により {} の読み取りに失敗しました。このMapperの読み取りはスキップします。" +
                                "例外メッセージを添えてIssueを作成していただけると、対応できるかもしれません。",
                        className, e);
                sqlReadStatus = SqlReadStatus.読み取り失敗あり;
            }
        }

        List<PersistenceOperation> list = new ArrayList<>();
        Collection<?> mappedStatements = config.getMappedStatements();
        logger.debug("MappedStatements: {}件", mappedStatements.size());
        for (Object obj : mappedStatements) {
            // config.getMappedStatementsにAmbiguityが入っていることがあったので型を確認する
            if (obj instanceof MappedStatement mappedStatement) {

                PersistenceOperationId persistenceOperationId = resolveStatementId(mappedStatement);

                Query query;
                try {
                    query = getQuery(mappedStatement);
                } catch (Exception e) {
                    logger.warn("クエリの取得に失敗しました", e);
                    sqlReadStatus = SqlReadStatus.読み取り失敗あり;
                    query = Query.unsupported();
                }

                // MyBatis上でのSQLの種類
                // https://mybatis.org/mybatis-3/ja/sqlmap-xml.html#sql_command_type
                var sqlCommandType = mappedStatement.getSqlCommandType();
                SqlType sqlType = switch (sqlCommandType) {
                    case SELECT -> SqlType.SELECT;
                    case INSERT -> SqlType.INSERT;
                    case UPDATE -> SqlType.UPDATE;
                    case DELETE -> SqlType.DELETE;
                    case UNKNOWN, FLUSH -> {
                        logger.warn("JIGではSQL Command Type {} はJIGでは対応していません。SELECTとして続行します。", sqlCommandType);
                        yield SqlType.SELECT;
                    }
                };
                PersistenceOperation myBatisStatement = PersistenceOperation.from(persistenceOperationId, query, sqlType);
                list.add(myBatisStatement);
            }
        }

        logger.debug("取得したSQL: {}件", list.size());

        List<PersistenceOperations> persistenceOperations = list.stream()
                .collect(Collectors.groupingBy(persistenceOperation -> persistenceOperation.persistenceOperationId().typeId()))
                .entrySet()
                .stream()
                .map(entry -> new PersistenceOperations(entry.getKey(), entry.getValue()))
                .toList();

        return new MyBatisReadResult(SqlStatements.from(persistenceOperations), sqlReadStatus);
    }

    /**
     * MyBatisのステートメント情報からSQLステートメントIDを作成する
     *
     * 以下のMapperXMLとMapperインタフェースの場合、ステートメントIDは `com.example.mybatis.ExampleMapper.selectAll` となる。
     *
     * <pre>
     * {@code
     * <mapper namespace="com.example.mybatis.ExampleMapper">
     *     <select id="selectAll">
     *         SELECT * FROM EXAMPLE
     *     </select>
     * </mapper>
     * }
     * </pre>
     * <pre>
     * {@code
     * package com.example.mybatis;
     * interface ExampleMapper {
     *     List selectAll();
     * }
     * }
     * </pre>
     */
    private static PersistenceOperationId resolveStatementId(MappedStatement mappedStatement) {
        var mappedStatementId = mappedStatement.getId();

        var namespaceIdSeparateIndex = mappedStatementId.lastIndexOf('.');
        if (namespaceIdSeparateIndex != -1) {
            return PersistenceOperationId.fromTypeIdAndName(
                    TypeId.valueOf(mappedStatementId.substring(0, namespaceIdSeparateIndex)),
                    mappedStatementId.substring(namespaceIdSeparateIndex + 1));
        } else {
            return PersistenceOperationId.fromTypeIdAndName(
                    // ダミー値を入れておく
                    TypeId.valueOf("jig.mybatis.unnamed"),
                    mappedStatementId);
        }
    }

    private Query getQuery(MappedStatement mappedStatement) throws NoSuchFieldException, IllegalAccessException {
        SqlSource sqlSource = mappedStatement.getSqlSource();

        if (!(sqlSource instanceof DynamicSqlSource dynamicSqlSource)) {
            return Query.from(mappedStatement.getBoundSql(null).getSql());
        }

        // 動的クエリ（XMLで組み立てるもの）をエミュレート

        Field rootSqlNode = DynamicSqlSource.class.getDeclaredField("rootSqlNode");
        rootSqlNode.setAccessible(true);
        SqlNode sqlNode = (SqlNode) rootSqlNode.get(dynamicSqlSource);

        if (sqlNode instanceof MixedSqlNode mixedSqlNode) {
            var sqlText = emulateSql(mixedSqlNode);
            logger.debug("動的SQLの組み立てをエミュレートしました。ID={}", mappedStatement.getId());
            logger.debug("組み立てたSQL: [{}]", sqlText);
            return Query.from(sqlText);
        }
        return Query.from(mappedStatement.getBoundSql(null).getSql());
    }

    private static String emulateSql(MixedSqlNode mixedSqlNode) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder sql = new StringBuilder();
        Field contents = mixedSqlNode.getClass().getDeclaredField("contents");
        contents.setAccessible(true);
        List<?> list = (List<?>) contents.get(mixedSqlNode);

        for (Object content : list) {
            if (content instanceof StaticTextSqlNode staticTextSqlNode) {
                Field text = StaticTextSqlNode.class.getDeclaredField("text");
                text.setAccessible(true);
                String textSql = (String) text.get(staticTextSqlNode);
                sql.append(textSql);
            }
        }

        return sql.toString().trim();
    }
}
