package jig.domain.model.list;

import java.util.List;

public class ModelMethods {

    List<ModelMethod> methods;

    public ModelMethods(List<ModelMethod> methods) {
        this.methods = methods;
    }

    public List<ModelMethod> list() {
        return methods;
    }
}
