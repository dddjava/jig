package jig.infrastructure.mybatis;

import jig.domain.model.datasource.Query;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlType;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.thing.Name;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class MyBatisSqlResolver {

    SqlRepository sqlRepository;
    TagRepository tagRepository;

    public MyBatisSqlResolver(SqlRepository sqlRepository, TagRepository tagRepository) {
        this.sqlRepository = sqlRepository;
        this.tagRepository = tagRepository;
    }

    public void resolve(Path projectPath) {
        try (Stream<Path> walk = Files.walk(projectPath)) {
            URL[] urls = walk.filter(Files::isDirectory)
                    .filter(path -> path.endsWith(Paths.get("build", "classes", "java", "main"))
                            || path.endsWith(Paths.get("build", "resources", "main"))
                    )
                    .map(Path::toAbsolutePath)
                    .map(path1 -> {
                        try {
                            return path1.toUri().toURL();
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
                Path rootPath = Paths.get(url.toURI());
                try (Stream<Path> walk = Files.walk(rootPath)) {
                    walk.filter(path -> path.toFile().isFile())
                            .map(rootPath::relativize)
                            .map(Path::toString)
                            .filter(path -> path.endsWith(".class"))
                            .forEach(name -> {
                                try {
                                    Class<?> mapperClass = classLoader.loadClass(
                                            name.substring(0, name.length() - 6).replace('/', '.'));
                                    mapperRegistry.addMapper(mapperClass);
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }

            for (Object obj : config.getMappedStatements()) {
                // Ambiguityが入っていることがあるので型を確認する
                if (obj instanceof MappedStatement) {
                    MappedStatement mappedStatement = (MappedStatement) obj;

                    Name name = new Name(mappedStatement.getId());
                    tagRepository.register(name, Tag.MAPPER_METHOD);

                    Query query = new Query(mappedStatement.getBoundSql(null).getSql());
                    SqlType sqlType = SqlType.valueOf(mappedStatement.getSqlCommandType().name());

                    Sql sql = new Sql(name, query, sqlType);
                    sqlRepository.register(sql);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
