package org.dddjava.jig.domain.model.implementation;

import java.util.List;
import java.util.stream.Collectors;

public class Specifications {
    private final List<Specification> list;

    public Specifications(List<Specification> list) {
        this.list = list;
    }

    public List<Specification> list() {
        return list;
    }

    public List<MethodSpecification> instanceMethodSpecifications() {
        return list.stream()
                .map(Specification::instanceMethodSpecifications)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
