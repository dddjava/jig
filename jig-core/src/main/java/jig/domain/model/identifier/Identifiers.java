package jig.domain.model.identifier;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class Identifiers {

    List<Identifier> list;

    public Identifiers(List<Identifier> list) {
        this.list = list;
    }

    public List<Identifier> list() {
        return list;
    }

    public static Collector<Identifier, ?, Identifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), Identifiers::new);
    }

    public boolean contains(Identifier identifier) {
        return list.contains(identifier);
    }

    public String asSimpleText() {
        return list.stream().map(Identifier::asSimpleText).collect(joining(",", "[", "]"));
    }
}
