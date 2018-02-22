package jig.domain.model.usage;

import jig.domain.model.dependency.FullQualifiedName;
import jig.domain.model.dependency.JapaneseName;

public class ServiceClass {

    FullQualifiedName name;
    JapaneseName japaneseName;

    ServiceMethods methods;

    DependentClasses dependents;

    public ServiceClass(FullQualifiedName name, JapaneseName japaneseName, ServiceMethods methods, DependentClasses dependents) {
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

    public ServiceMethods methods() {
        return methods;
    }

    public DependentClasses dependents() {
        return dependents;
    }
}
