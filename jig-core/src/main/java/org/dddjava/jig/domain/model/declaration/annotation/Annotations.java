package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * アノテーション一覧
 */
public class Annotations {
    List<Annotation> list;

    Annotations(List<Annotation> list) {
        this.list = list;
    }

    public Annotations filterAny(TypeIdentifier... typeIdentifiers) {
        Collection<TypeIdentifier> annotations = Arrays.asList(typeIdentifiers);

        List<Annotation> list = this.list.stream()
                .filter(e -> annotations.contains(e.annotationType))
                .collect(toList());
        return new Annotations(list);
    }

    public List<String> descriptionTextsOf(String name) {
        return list.stream()
                .map(e -> e.description.textOf(name))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
