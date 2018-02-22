package jig.domain.model.usage;

import java.util.List;

public class DependentClasses {
    List<Class<?>> list;

    public DependentClasses(List<Class<?>> list) {
        this.list = list;
    }

    public List<Class<?>> list() {
        return list;
    }
}
