package jig.cli.infrastructure.usage;

import jig.analizer.javaparser.PackageInfoParser;
import jig.domain.model.dependency.FullQualifiedName;
import jig.domain.model.dependency.JapaneseNameRepository;
import jig.domain.model.usage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

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

    ModelTypeRepositoryImpl(@Value("${target.class}") String targetClasspath, @Value("${target.source}") String sourcePath) {
        Path path = Paths.get(targetClasspath);

        PackageInfoParser packageInfoParser = new PackageInfoParser(Paths.get(sourcePath));
        JapaneseNameRepository japaneseNames = packageInfoParser.parseClass();

        try (URLClassLoader loader = new URLClassLoader(new URL[]{path.toUri().toURL()});
             Stream<Path> walk = Files.walk(path)) {

            classes = walk.filter(p -> p.toString().endsWith("Service.class"))
                    .map(path::relativize)
                    .map(Path::toString)
                    .map(str -> str.replace(".class", "").replace("/", "."))
                    .map(className -> {
                        try {
                            return loader.loadClass(className);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .map(serviceClass -> {
                        FullQualifiedName fullQualifiedName = new FullQualifiedName(serviceClass.getCanonicalName());
                        return new ModelType(
                                fullQualifiedName,
                                japaneseNames.get(fullQualifiedName),
                                ModelMethods.from(serviceClass),
                                DependentTypes.from(serviceClass)
                        );
                    }).collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
