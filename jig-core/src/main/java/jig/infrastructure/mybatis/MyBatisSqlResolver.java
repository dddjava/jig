package jig.infrastructure.mybatis;

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
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
public class MyBatisSqlResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisSqlResolver.class);

    SqlRepository sqlRepository;
    JigPaths jigPaths;

    public MyBatisSqlResolver(SqlRepository sqlRepository, JigPaths jigPaths) {
        this.sqlRepository = sqlRepository;
        this.jigPaths = jigPaths;
    }

    public void resolve(Path projectPath) {
        try (Stream<Path> walk = Files.walk(projectPath)) {
            URL[] urls = walk.filter(Files::isDirectory)
                    .filter(jigPaths::isGradleClassPathRootDirectory)
                    .map(Path::toAbsolutePath)
                    .map(path -> {
                        try {
                            return path.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toArray(URL[]::new);

            resolve(urls);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void resolve(URL... urls) {
        try (URLClassLoader classLoader = new URLClassLoader(urls, MapperRegistry.class.getClassLoader())) {
            Resources.setDefaultClassLoader(classLoader);

            Configuration config = new Configuration();

            MapperRegistry mapperRegistry = new MapperRegistry(config);
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
                                    mapperRegistry.addMapper(mapperClass);
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

                    SqlIdentifier sqlIdentifier= new SqlIdentifier(mappedStatement.getId());

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

                    LOGGER.warn("DynamicSqlSourceを組み立て: " + mappedStatement.getId());
                    return new Query(sql.toString().trim());
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
