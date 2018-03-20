package jig.domain.model.identifier;

import java.util.Collections;
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

    public String asCompressText() {
        return list.stream().map(Identifier::asCompressText).collect(joining(",", "[", "]"));
    }

    public static Identifiers empty() {
        return new Identifiers(Collections.emptyList());
    }
}
