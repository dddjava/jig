package jig.domain.model.usage;

import jig.domain.model.dependency.FullQualifiedName;
import jig.domain.model.dependency.JapaneseName;

public class ModelType {

    FullQualifiedName name;
    JapaneseName japaneseName;

    ModelMethods methods;

    DependentTypes dependents;

    public ModelType(FullQualifiedName name, JapaneseName japaneseName, ModelMethods methods, DependentTypes dependents) {
        this.name = name;
        this.japaneseName = japaneseName;
        this.methods = methods;
        this.dependents = dependents;
    }

    public FullQualifiedName name() {
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
