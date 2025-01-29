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
import org.dddjava.jig.domain.model.data.classes.rdbaccess.*;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.mybatis.MyBatisStatementsReader;
import org.dddjava.jig.domain.model.sources.mybatis.SqlSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 本クラスではMyBatisの内部APIを使用してSQLを取得しています。
 * ごくシンプルな使い方のみを想定しているため、高度な使い方をしている場合は正確な情報が取得できません。
 *
 * クラスパスにMapperが依存しているクラスが存在しない場合、MyBatisがMapperを読み取れないため解析できません。
 * それでも出力したい場合は、実行時に該当のクラスをクラスパスに含めてください。
 */
public class MyBatisMyBatisStatementsReader implements MyBatisStatementsReader {

    private static final Logger logger = LoggerFactory.getLogger(MyBatisMyBatisStatementsReader.class);

    @Override
    public MyBatisStatements readFrom(Sources sources) {
        SqlSources sqlSources = SqlSources.from(sources);
        // 該当なしの場合に余計なClassLoader生成やMyBatisの初期化を行わない
        if (sqlSources.classNames().isEmpty()) return new MyBatisStatements(SqlReadStatus.成功);

        URL[] classLocationUrls = sqlSources.sourceBasePaths().classSourceBasePaths().stream()
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        logger.warn("pathのURLへの変換に失敗しました。{}を読み飛ばします。", path, e);
                        return null;
                    }
                })
                .filter(url -> url != null)
                .toArray(URL[]::new);
        try (URLClassLoader classLoader = new URLClassLoader(classLocationUrls, Configuration.class.getClassLoader())) {
            Resources.setDefaultClassLoader(classLoader);

            return extractSql(sqlSources, classLoader);
        } catch (IOException e) {
            logger.warn("SQLファイルの読み込みでIO例外が発生しました。" +
                    "すべてのSQLは認識されません。リポジトリのCRUDは出力されませんが、他の出力には影響ありません。", e);
            return new MyBatisStatements(SqlReadStatus.失敗);
        } catch (PersistenceException e) {
            logger.warn("SQL読み込み中にMyBatisに関する例外が発生しました。" +
                    "すべてのSQLは認識されません。リポジトリのCRUDは出力されませんが、他の出力には影響ありません。" +
                    "この例外は #228 #710 で確認していますが、情報が不足しています。発生条件をやスタックトレース等の情報をいただけると助かります。", e);
            return new MyBatisStatements(SqlReadStatus.失敗);
        }
    }

    private MyBatisStatements extractSql(SqlSources sqlSources, ClassLoader classLoader) {
        SqlReadStatus sqlReadStatus = SqlReadStatus.成功;

        Configuration config = new Configuration();
        for (String className : sqlSources.classNames()) {
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

        List<MyBatisStatement> list = new ArrayList<>();
        Collection<?> mappedStatements = config.getMappedStatements();
        logger.debug("MappedStatements: {}件", mappedStatements.size());
        for (Object obj : mappedStatements) {
            // config.getMappedStatementsにAmbiguityが入っていることがあったので型を確認する
            if (obj instanceof MappedStatement mappedStatement) {

                MyBatisStatementId myBatisStatementId = new MyBatisStatementId(mappedStatement.getId());

                Query query;
                try {
                    query = getQuery(mappedStatement);
                } catch (Exception e) {
                    logger.warn("クエリの取得に失敗しました", e);
                    sqlReadStatus = SqlReadStatus.読み取り失敗あり;
                    query = Query.unsupported();
                }

                SqlType sqlType = SqlType.valueOf(mappedStatement.getSqlCommandType().name());
                MyBatisStatement myBatisStatement = new MyBatisStatement(myBatisStatementId, query, sqlType);
                list.add(myBatisStatement);
            }
        }

        logger.debug("取得したSQL: {}件", list.size());
        return new MyBatisStatements(list, sqlReadStatus);
    }

    private Query getQuery(MappedStatement mappedStatement) throws NoSuchFieldException, IllegalAccessException {
        SqlSource sqlSource = mappedStatement.getSqlSource();

        if (!(sqlSource instanceof DynamicSqlSource dynamicSqlSource)) {
            return new Query(mappedStatement.getBoundSql(null).getSql());
        }

        // 動的クエリ（XMLで組み立てるもの）をエミュレート

        Field rootSqlNode = DynamicSqlSource.class.getDeclaredField("rootSqlNode");
        rootSqlNode.setAccessible(true);
        SqlNode sqlNode = (SqlNode) rootSqlNode.get(dynamicSqlSource);

        if (sqlNode instanceof MixedSqlNode mixedSqlNode) {
            var sqlText = emulateSql(mixedSqlNode);
            logger.debug("動的SQLの組み立てをエミュレートしました。ID={}", mappedStatement.getId());
            logger.debug("組み立てたSQL: [{}]", sqlText);
            return new Query(sqlText);
        }
        return new Query(mappedStatement.getBoundSql(null).getSql());
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
