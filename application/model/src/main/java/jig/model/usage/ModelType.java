package jig.model.usage;

import jig.model.tag.JapaneseName;
import jig.model.thing.Name;

public class ModelType {

    Name name;
    JapaneseName japaneseName;

    ModelMethods methods;

    DependentTypes dependents;

    public ModelType(Name name, JapaneseName japaneseName, ModelMethods methods, DependentTypes dependents) {
        this.name = name;
        this.japaneseName = japaneseName;
        this.methods = methods;
        this.dependents = dependents;
    }

    public Name name() {
        return name;
    }

    public JapaneseName japaneseName() {
        return japaneseName;
    }

    public ModelMethods methods() {
        return methods;
    }

    public DependentTypes dependents() {
        return dependents;
    }
}
