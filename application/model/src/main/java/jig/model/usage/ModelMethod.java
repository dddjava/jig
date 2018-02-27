package jig.model.usage;

import java.lang.reflect.Method;

public class ModelMethod {

    private final Method method;

    public ModelMethod(Method method) {
        this.method = method;
    }

    public String name() {
        return method.getName();
    }

    public Class<?> returnType() {
        return method.getReturnType();
    }

    public Class<?>[] parameters() {
        return method.getParameterTypes();
    }
}
