package jig.domain.model.usage;

import java.util.List;

public class ServiceClasses {

    private final List<ServiceClass> list;

    public ServiceClasses(List<ServiceClass> list) {
        this.list = list;
    }

    public List<ServiceClass> list() {
        return list;
    }
}
