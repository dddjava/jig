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

    public void resolve(URL... urls) {
        try (URLClassLoader classLoader = new URLClassLoader(urls)) {
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
                    tagRepository.register(Tag.MAPPER_METHOD, name);

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
