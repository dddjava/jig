package jig.domain.model.specification;

import java.util.List;

public class Specifications {
    private final List<Specification> list;

    public Specifications(List<Specification> list) {
        this.list = list;
    }

    public List<Specification> list() {
        return list;
    }
}
