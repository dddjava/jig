package jig.domain.model.list;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

public class ModelMethods {

    private static final Logger LOGGER = Logger.getLogger(ModelMethods.class.getName());

    List<ModelMethod> methods;

    public ModelMethods(List<ModelMethod> methods) {
        this.methods = methods;
    }

    public List<ModelMethod> list() {
        return methods;
    }

    public static ModelMethods from(Class<?> clz) {
        return new ModelMethods(
                Arrays.stream(getDeclaredMethods(clz))
                        .map(ModelMethod::new)
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

    public boolean empty() {
        return methods.isEmpty();
    }
}
