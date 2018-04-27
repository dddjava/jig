package org.dddjava.jig.infrastructure.mybatis;

import org.dddjava.jig.domain.model.datasource.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.dddjava.jig.domain.model.datasource.SqlSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Component
public class MyBatisSqlReader implements SqlReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisSqlReader.class);

    @Override
    public Sqls readFrom(SqlSources sqlSources) {
        try (URLClassLoader classLoader = new URLClassLoader(sqlSources.urls(), Configuration.class.getClassLoader())) {
            Resources.setDefaultClassLoader(classLoader);

            Configuration config = configuration(sqlSources, classLoader);
            return extractSql(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Configuration configuration(SqlSources sqlSources, URLClassLoader classLoader) {
        Configuration config = new Configuration();
        for (String className : sqlSources.classNames()) {
            try {
                Class<?> mapperClass = classLoader.loadClass(className);
                config.addMapper(mapperClass);
            } catch (NoClassDefFoundError e) {
                LOGGER.warn("Mapperが未知のクラスに依存しているため読み取れませんでした。 読み取りに失敗したclass={}, メッセージ={}",
                        className, e.getLocalizedMessage());
            } catch (Exception e) {
                LOGGER.warn("Mapperの取り込みに失敗", e);
            }
        }
        return config;
    }

    private Sqls extractSql(Configuration config) {
        List<Sql> list = new ArrayList<>();
        for (Object obj : config.getMappedStatements()) {
            // Ambiguityが入っていることがあるので型を確認する
            if (obj instanceof MappedStatement) {
                MappedStatement mappedStatement = (MappedStatement) obj;

                SqlIdentifier sqlIdentifier = new SqlIdentifier(mappedStatement.getId());

                Query query = getQuery(mappedStatement);
                SqlType sqlType = SqlType.valueOf(mappedStatement.getSqlCommandType().name());

                Sql sql = new Sql(sqlIdentifier, query, sqlType);
                list.add(sql);
            }
        }
        return new Sqls(list);
    }

    private Query getQuery(MappedStatement mappedStatement) {
        try {
            SqlSource sqlSource = mappedStatement.getSqlSource();

            if (sqlSource instanceof DynamicSqlSource) {
                DynamicSqlSource dynamicSqlSource = DynamicSqlSource.class.cast(sqlSource);

                Field rootSqlNode = DynamicSqlSource.class.getDeclaredField("rootSqlNode");
                rootSqlNode.setAccessible(true);
                SqlNode sqlNode = (SqlNode) rootSqlNode.get(dynamicSqlSource);


                if (sqlNode instanceof MixedSqlNode) {
                    StringBuilder sql = new StringBuilder();
                    MixedSqlNode mixedSqlNode = MixedSqlNode.class.cast(sqlNode);
                    Field contents = mixedSqlNode.getClass().getDeclaredField("contents");
                    contents.setAccessible(true);
                    List list = (List) contents.get(mixedSqlNode);

                    for (Object content : list) {
                        if (content instanceof StaticTextSqlNode) {
                            StaticTextSqlNode staticTextSqlNode = StaticTextSqlNode.class.cast(content);
                            Field text = StaticTextSqlNode.class.getDeclaredField("text");
                            text.setAccessible(true);
                            String textSql = (String) text.get(staticTextSqlNode);
                            sql.append(textSql);
                        }
                    }

                    String sqlText = sql.toString().trim();
                    LOGGER.debug("動的SQLの組み立てをエミュレートしました。ID={}", mappedStatement.getId());
                    LOGGER.debug("組み立てたSQL: [{}]", sqlText);
                    return new Query(sqlText);
                }
            } else {
                return new Query(mappedStatement.getBoundSql(null).getSql());
            }

            LOGGER.warn("クエリの取得に失敗しました");
            return Query.unsupported();
        } catch (Exception e) {
            LOGGER.warn("クエリの取得に失敗しました", e);
            return Query.unsupported();
        }
    }
}
