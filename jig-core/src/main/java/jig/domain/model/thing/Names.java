package jig.domain.model.thing;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class Names {

    List<Identifier> list;

    public Names(List<Identifier> list) {
        this.list = list;
    }

    public List<Identifier> list() {
        return list;
    }

    public static Collector<Identifier, ?, Names> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), Names::new);
    }

    public boolean contains(Identifier identifier) {
        return list.contains(identifier);
    }

    public String asCompressText() {
        return list.stream().map(Identifier::asCompressText).collect(joining(",", "[", "]"));
    }

    public static Names empty() {
        return new Names(Collections.emptyList());
    }
}
