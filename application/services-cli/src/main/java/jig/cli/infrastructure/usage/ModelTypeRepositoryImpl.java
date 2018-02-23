package jig.cli.infrastructure.usage;

import jig.domain.model.usage.ModelType;
import jig.domain.model.usage.ModelTypeRepository;
import jig.domain.model.usage.ModelTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Path path = Paths.get(targetClasspath);

        try (URLClassLoader loader = new URLClassLoader(new URL[]{path.toUri().toURL()});
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
