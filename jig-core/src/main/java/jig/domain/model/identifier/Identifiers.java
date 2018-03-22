package jig.domain.model.identifier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class Identifiers {

    List<Identifier> list;

    public Identifiers(List<Identifier> list) {
        this.list = list;
        list.sort(Comparator.comparing(Identifier::value));
    }

    public List<Identifier> list() {
        return list;
    }

    public static Collector<Identifier, ?, Identifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), Identifiers::new);
    }

    public String asText() {
        return list.stream().map(Identifier::value).collect(joining(","));
    }

    public String asSimpleText() {
        return list.stream().map(Identifier::asSimpleText).collect(joining(","));
    }

    public Identifiers merge(Identifiers other) {
        return Stream.concat(list.stream(), other.list.stream()).collect(Identifiers.collector());
    }
}
