package jig.domain.model.identifier;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class Identifiers {

    List<TypeIdentifier> list;

    public Identifiers(List<TypeIdentifier> list) {
        this.list = list;
        list.sort(Comparator.comparing(TypeIdentifier::value));
    }

    public List<TypeIdentifier> list() {
        return list;
    }

    public Identifiers filter(Predicate<TypeIdentifier> condition) {
        return list.stream().filter(condition).collect(collector());
    }

    public static Collector<TypeIdentifier, ?, Identifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), Identifiers::new);
    }

    public String asText() {
        return list.stream().map(TypeIdentifier::value).collect(joining(","));
    }

    public String asSimpleText() {
        return list.stream().map(TypeIdentifier::asSimpleText).collect(joining(","));
    }

    public Identifiers merge(Identifiers other) {
        return Stream.concat(list.stream(), other.list.stream()).collect(Identifiers.collector());
    }
}
