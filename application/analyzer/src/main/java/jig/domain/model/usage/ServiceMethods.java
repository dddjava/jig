package jig.domain.model.usage;

import java.util.List;

public class ServiceMethods {

    List<ServiceMethod> methods;

    public ServiceMethods(List<ServiceMethod> methods) {
        this.methods = methods;
    }

    public List<ServiceMethod> list() {
        return methods;
    }
}
