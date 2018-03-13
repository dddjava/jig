package jig.infrastructure.reflection;

import jig.domain.model.list.*;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelTypeClassLoader implements ModelTypeRepository {

    private static final Logger LOGGER = Logger.getLogger(ModelTypeClassLoader.class.getName());

    private List<ModelType> classes;

    RelationRepository relationRepository;

    @Override
    public ModelTypes find(ModelKind modelKind) {
        return new ModelTypes(classes.stream().filter(modelKind::correct).collect(toList()));
    }

    public ModelTypeClassLoader(String targetClasspath, RelationRepository relationRepository) {
        this.relationRepository = relationRepository;
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

            classes = walk
                    .filter(p -> p.toString().endsWith(".class"))
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
                    .peek(this::registerRelation)
                    .map(clz -> new ModelType(new Name(clz), toModelMethod(clz)))
                    .collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ModelMethods toModelMethod(Class<?> clz) {
        return new ModelMethods(
                Arrays.stream(getDeclaredMethods(clz))
                        .map(this::getModelMethod)
                        .collect(toList()));
    }

    private ModelMethod getModelMethod(Method method) {
        return new ModelMethod(
                method.getName(),
                new Name(method.getReturnType()),
                Arrays.stream(method.getParameterTypes())
                        .map(Name::new)
                        .collect(toList()));
    }

    private static Method[] getDeclaredMethods(Class<?> clz) {
        try {
            return clz.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            LOGGER.warning("依存クラスが見つからないためメソッドが取得できませんでした。 class:" + clz + " message:" + e.getMessage());
            return new Method[0];
        }
    }

    public void registerRelation(Class<?> clz) {
        for (Field field : clz.getDeclaredFields()) {
            Relation relation = new Relation(
                    new Thing(new Name(clz)),
                    new Thing(new Name(field.getType()))
            );
            relationRepository.persist(relation);
        }
    }
}
