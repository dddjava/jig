package org.dddjava.jig.domain.model.implementation.bytecode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * モデルの実装一式
 */
public class Implementations {
    private final List<Implementation> list;

    public Implementations(List<Implementation> list) {
        this.list = list;
    }

    public List<Implementation> list() {
        return list;
    }

    public List<MethodImplementation> instanceMethodSpecifications() {
        return list.stream()
                .map(Implementation::instanceMethodSpecifications)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
