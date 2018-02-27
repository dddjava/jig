package jig.model.usage;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ModelMethods {

    List<ModelMethod> methods;

    public ModelMethods(List<ModelMethod> methods) {
        this.methods = methods;
    }

    public List<ModelMethod> list() {
        return methods;
    }

    public static ModelMethods from(Class<?> serviceClass) {
        return new ModelMethods(
                Arrays.stream(serviceClass.getDeclaredMethods())
                        .map(ModelMethod::new)
                        .collect(toList()));
    }
}
