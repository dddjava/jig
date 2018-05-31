package org.dddjava.jig.domain.model.identifier.type;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * 型の識別子一覧
 */
public class TypeIdentifiers {

    List<TypeIdentifier> identifiers;

    public TypeIdentifiers(List<TypeIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<TypeIdentifier> list() {
        ArrayList<TypeIdentifier> list = new ArrayList<>(this.identifiers);
        list.sort(Comparator.comparing(TypeIdentifier::fullQualifiedName));
        return list;
    }

    public Set<TypeIdentifier> set() {
        return new HashSet<>(identifiers);
    }

    public static Collector<TypeIdentifier, ?, TypeIdentifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), TypeIdentifiers::new);
    }

    public String asSimpleText() {
        return identifiers.stream()
                .distinct()
                .map(TypeIdentifier::asSimpleText)
                .sorted()
                .collect(joining(", ", "[", "]"));
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return identifiers.contains(typeIdentifier);
    }

    public boolean empty() {
        return identifiers.isEmpty();
    }
}
