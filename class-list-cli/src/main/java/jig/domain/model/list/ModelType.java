package jig.domain.model.list;

import jig.domain.model.thing.Name;

public class ModelType {

    Name name;

    ModelMethods methods;

    public ModelType(Name name, ModelMethods methods) {
        this.name = name;
        this.methods = methods;
    }

    public Name name() {
        return name;
    }

    public ModelMethods methods() {
        return methods;
    }
}
