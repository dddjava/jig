package jig.domain.model.identifier.type;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

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

    public String asText() {
        return identifiers.stream().map(TypeIdentifier::fullQualifiedName).distinct().collect(joining(", ", "[", "]"));
    }

    public String asSimpleText() {
        return identifiers.stream().map(TypeIdentifier::asSimpleText).distinct().collect(joining(", ", "[", "]"));
    }

    public TypeIdentifiers merge(TypeIdentifiers other) {
        return Stream.concat(identifiers.stream(), other.identifiers.stream()).collect(TypeIdentifiers.collector());
    }
}
