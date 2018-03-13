package jig.infrastructure.mybatis;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlIdentifier;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlType;
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
import java.util.Collection;
import java.util.stream.Stream;

public class MyBatisSqlResolver {

    SqlRepository sqlRepository;

    public MyBatisSqlResolver(SqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
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

            Collection<?> mappedStatements = config.getMappedStatements();
            for (Object obj : mappedStatements) {
                // Ambiguityが入っていることがあるので型を確認する
                if (obj instanceof MappedStatement) {
                    MappedStatement mappedStatement = (MappedStatement) obj;
                    Sql sql = new Sql(
                            new SqlIdentifier(mappedStatement.getId()),
                            mappedStatement.getBoundSql(null).getSql(),
                            SqlType.valueOf(mappedStatement.getSqlCommandType().name()));
                    sqlRepository.register(sql);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
