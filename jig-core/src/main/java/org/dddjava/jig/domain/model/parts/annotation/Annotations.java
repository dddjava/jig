package org.dddjava.jig.domain.model.parts.annotation;

import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * アノテーション一覧
 */
public class Annotations {
    List<Annotation> list;

    public Annotations(List<Annotation> list) {
        this.list = list;
    }

    public Annotations filterAny(TypeIdentifier... typeIdentifiers) {
        Collection<TypeIdentifier> annotations = Arrays.asList(typeIdentifiers);

        List<Annotation> list = this.list.stream()
                .filter(e -> annotations.contains(e.typeIdentifier))
                .collect(toList());
        return new Annotations(list);
    }

    public List<Annotation> list() {
        return list;
    }
}
