package jig.infrastructure.mybatis;

import jig.domain.model.project.ProjectLocation;
import jig.domain.model.datasource.*;
import jig.infrastructure.JigPaths;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Component
public class MyBatisSqlReader implements SqlReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisSqlReader.class);

    SqlRepository sqlRepository;
    JigPaths jigPaths;

    public MyBatisSqlReader(SqlRepository sqlRepository, JigPaths jigPaths) {
        this.sqlRepository = sqlRepository;
        this.jigPaths = jigPaths;
    }

    @Override
    public void readFrom(ProjectLocation projectLocation) {
        URL[] urls = Arrays.stream(jigPaths.extractClassPath(projectLocation.getValue()))
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                }).toArray(URL[]::new);
        resolve(urls);
    }

    void resolve(URL... urls) {
        try (URLClassLoader classLoader = new URLClassLoader(urls, MapperRegistry.class.getClassLoader())) {
            Resources.setDefaultClassLoader(classLoader);

            Configuration config = new Configuration();

            for (URL url : classLoader.getURLs()) {
                LOGGER.info("Mapper取り込み: " + url);
                Path rootPath = Paths.get(url.toURI());
                try (Stream<Path> walk = Files.walk(rootPath)) {
                    walk.filter(path -> path.toFile().isFile())
                            .map(rootPath::relativize)
                            .filter(jigPaths::isMapperClassFile)
                            .forEach(path -> {
                                try {
                                    String className = jigPaths.toClassName(path);
                                    Class<?> mapperClass = classLoader.loadClass(className);
                                    config.addMapper(mapperClass);
                                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                                    LOGGER.warn("クラスロードに失敗: path:{}", path, e);
                                } catch (Exception e) {
                                    LOGGER.warn("Mapperの取り込みに失敗", e);
                                }
                            });
                }
            }

            for (Object obj : config.getMappedStatements()) {
                // Ambiguityが入っていることがあるので型を確認する
                if (obj instanceof MappedStatement) {
                    MappedStatement mappedStatement = (MappedStatement) obj;

                    SqlIdentifier sqlIdentifier = new SqlIdentifier(mappedStatement.getId());

                    Query query = getQuery(mappedStatement);
                    SqlType sqlType = SqlType.valueOf(mappedStatement.getSqlCommandType().name());

                    Sql sql = new Sql(sqlIdentifier, query, sqlType);
                    sqlRepository.register(sql);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
                    LOGGER.debug("DynamicSqlSource: {} [{}]", mappedStatement.getId(), sqlText);
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
