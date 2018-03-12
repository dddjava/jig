package jig.domain.model.usage;

import jig.domain.model.tag.JapaneseName;
import jig.domain.model.thing.Name;

public class ModelType {

    Name name;
    JapaneseName japaneseName;

    ModelMethods methods;

    public ModelType(Name name, JapaneseName japaneseName, ModelMethods methods) {
        this.name = name;
        this.japaneseName = japaneseName;
        this.methods = methods;
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
}
