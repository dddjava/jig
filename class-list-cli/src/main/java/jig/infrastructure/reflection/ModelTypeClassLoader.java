package jig.infrastructure.reflection;

import jig.domain.model.list.ModelMethod;
import jig.domain.model.list.ModelMethods;
import jig.domain.model.list.ModelType;
import jig.domain.model.list.ModelTypeRepository;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelTypeClassLoader {

    private static final Logger LOGGER = Logger.getLogger(ModelTypeClassLoader.class.getName());

    private final URL[] urls;
    private final ModelTypeRepository modelTypeRepository;

    RelationRepository relationRepository;

    public ModelTypeClassLoader(String targetClasspath, RelationRepository relationRepository, ModelTypeRepository modelTypeRepository) {
        this.modelTypeRepository = modelTypeRepository;
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
        try {
            Path path = Paths.get(urls[0].toURI());
            try (Stream<Path> walk = Files.walk(path)) {
                walk.filter(p -> p.toString().endsWith(".class"))
                        .map(path::relativize)
                        .map(Path::toString)
                        .map(str -> str.replace(".class", "").replace(File.separator, "."))
                        .map(this::toModelType)
                        .forEach(modelTypeRepository::register);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private ModelType toModelType(String className) {
        try (URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader())) {
            Class<?> clz = loader.loadClass(className);

            registerRelation(clz);

            List<ModelMethod> list = Arrays.stream(getDeclaredMethods(clz))
                    .map(this::toModelMethod)
                    .sorted(Comparator.comparing(ModelMethod::name))
                    .collect(toList());
            ModelMethods methods = new ModelMethods(list);

            return new ModelType(new Name(clz), methods);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ModelMethod toModelMethod(Method method) {
        return new ModelMethod(
                methodName(method),
                getReturnTypeName(method),
                parameterNames(method));
    }

    private List<Name> parameterNames(Method method) {
        return Arrays.stream(method.getParameterTypes()).map(Name::new).collect(toList());
    }

    private Name getReturnTypeName(Method method) {
        return new Name(method.getReturnType());
    }

    private String methodName(Method method) {
        return method.getName();
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
        for (Field field : clz.getDeclaredFields()) {
            Relation relation = new Relation(
                    new Thing(new Name(clz)),
                    new Thing(new Name(field.getType()))
            );
            relationRepository.persist(relation);
        }
    }
}
