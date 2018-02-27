package jig.cli.infrastructure.usage;

import jig.model.usage.ModelType;
import jig.model.usage.ModelTypeRepository;
import jig.model.usage.ModelTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Repository
public class ModelTypeRepositoryImpl implements ModelTypeRepository {

    private List<ModelType> classes;

    @Override
    public ModelTypes findAll() {
        return new ModelTypes(classes);
    }

    public ModelTypeRepositoryImpl(ModelTypeFactory factory, @Value("${target.class}") String targetClasspath) {
        URL[] urls = Arrays.stream(targetClasspath.split(":"))
                .map(Paths::get)
                .map(Path::toUri)
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .toArray(URL[]::new);
        Path path = Paths.get(targetClasspath.split(":")[0]);

        try (URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
             Stream<Path> walk = Files.walk(path)) {

            classes = walk.filter(factory::isTargetClass)
                    .map(path::relativize)
                    .map(Path::toString)
                    .map(str -> str.replace(".class", "").replace(File.separator, "."))
                    .map(className -> {
                        try {
                            return loader.loadClass(className);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .map(factory::toModelType)
                    .collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
