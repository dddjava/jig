package jig.infrastructure.reflection;

import jig.domain.model.list.ModelMethod;
import jig.domain.model.list.ModelMethods;
import jig.domain.model.list.ModelType;
import jig.domain.model.list.ModelTypeRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelTypeClassLoader {

    private static final Logger LOGGER = Logger.getLogger(ModelTypeClassLoader.class.getName());

    private final URL[] urls;
    private final ThingRepository thingRepository;
    private final ModelTypeRepository modelTypeRepository;

    RelationRepository relationRepository;

    public ModelTypeClassLoader(String targetClasspath, ThingRepository thingRepository, RelationRepository relationRepository, ModelTypeRepository modelTypeRepository) {
        this.thingRepository = thingRepository;
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
                        .forEach(className -> {
                            ModelType modelType = toModelType(className);
                            modelTypeRepository.register(modelType);
                        });
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

            Name name = new Name(clz);
            thingRepository.register(new Thing(name, ThingType.CLASS));

            return new ModelType(name, methods);
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
        try {
            for (Field field : clz.getDeclaredFields()) {
                Thing from = new Thing(new Name(clz), ThingType.CLASS);
                Thing to = new Thing(new Name(field.getType()), ThingType.CLASS);
                thingRepository.register(from);
                thingRepository.register(to);

                Relation relation = RelationType.FIELD.create(from.name(), to.name());
                relationRepository.regisger(relation);
            }
        } catch (NoClassDefFoundError e) {
            LOGGER.warning("依存クラスが見つからないためフィールドが取得できませんでした。 class:" + clz + " message:" + e.getMessage());
        }
    }
}
