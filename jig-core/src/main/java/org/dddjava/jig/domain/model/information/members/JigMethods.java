package org.dddjava.jig.domain.model.information.members;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * メソッド一覧
 */
public record JigMethods(Collection<JigMethod> methods) {

    public List<JigMethod> list() {
        return stream()
                .sorted(Comparator
                        .comparing(JigMethod::visibility)
                        .thenComparing(jigMethod -> jigMethod.jigMethodId().value()))
                .toList();
    }

    public boolean isEmpty() {
        return methods.isEmpty();
    }

    public JigMethods filterProgrammerDefined() {
        return new JigMethods(stream()
                .filter(jigMethod -> jigMethod.isProgrammerDefined())
                .toList());
    }

    public JigMethods excludeObjectMethods() {
        return new JigMethods(stream()
                .filter(jigMethod -> !jigMethod.isObjectMethod())
                .toList());
    }

    public Stream<JigMethod> stream() {
        return methods.stream();
    }

}
