package jig.infrastructure.reflection;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;
import jig.domain.model.thing.ThingRepository;
import jig.domain.model.thing.ThingType;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

public class ModelTypeClassLoader {

    private static final Logger LOGGER = Logger.getLogger(ModelTypeClassLoader.class.getName());

    URL[] urls;
    ThingRepository thingRepository;
    RelationRepository relationRepository;

    public ModelTypeClassLoader(String targetClasspath, ThingRepository thingRepository, RelationRepository relationRepository) {
        this.thingRepository = thingRepository;
        this.relationRepository = relationRepository;

        this.urls = Arrays.stream(targetClasspath.split(":"))
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
    }

    public void load() {
        Arrays.stream(urls)
                .map(url -> {
                    try {
                        return url.toURI();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(Paths::get)
                .forEach(this::load);
    }

    private void load(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.filter(p -> p.toString().endsWith(".class"))
                    .map(path::relativize)
                    .map(Path::toString)
                    .map(str -> str.replace(".class", "").replace(File.separator, "."))
                    .forEach(this::analyze);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void analyze(String className) {
        try (URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader())) {
            Class<?> clz = loader.loadClass(className);
            Name name = new Name(clz);
            thingRepository.register(new Thing(name, ThingType.TYPE));

            registerRelation(clz);

            for (Method method : getDeclaredMethods(clz)) {
                Name methodName = new Name(className + "." + method.getName());

                thingRepository.register(new Thing(methodName, ThingType.METHOD));
                relationRepository.register(RelationType.METHOD.create(name, methodName));

                Name returnTypeName = new Name(method.getReturnType());
                thingRepository.register(new Thing(returnTypeName, ThingType.TYPE));
                relationRepository.register(RelationType.METHOD_RETURN_TYPE.create(methodName, returnTypeName));
                parameterNames(method).forEach(parameterName -> {
                    thingRepository.register(new Thing(parameterName, ThingType.TYPE));
                    relationRepository.register(RelationType.METHOD_PARAMETER.create(methodName, parameterName));
                });
            }

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Name> parameterNames(Method method) {
        return Arrays.stream(method.getParameterTypes()).map(Name::new).collect(toList());
    }

    private static Method[] getDeclaredMethods(Class<?> clz) {
        try {
            return clz.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            LOGGER.warning("依存クラスが見つからないためメソッドが取得できませんでした。 class:" + clz + " message:" + e.getMessage());
            return new Method[0];
        }
    }

    private void registerRelation(Class<?> clz) {
        try {
            for (Field field : clz.getDeclaredFields()) {
                Thing from = new Thing(new Name(clz), ThingType.TYPE);
                Thing to = new Thing(new Name(field.getType()), ThingType.TYPE);
                thingRepository.register(from);
                thingRepository.register(to);

                Relation relation = RelationType.FIELD.create(from.name(), to.name());
                relationRepository.register(relation);
            }
        } catch (NoClassDefFoundError e) {
            LOGGER.warning("依存クラスが見つからないためフィールドが取得できませんでした。 class:" + clz + " message:" + e.getMessage());
        }
    }
}
