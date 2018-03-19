package jig.domain.model.thing;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class Names {

    List<Name> list;

    public Names(List<Name> list) {
        this.list = list;
    }

    public List<Name> list() {
        return list;
    }

    public static Collector<Name, ?, Names> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), Names::new);
    }

    public boolean contains(Name name) {
        return list.contains(name);
    }

    public String asCompressText() {
        return list.stream().map(Name::asCompressText).collect(joining(",", "[", "]"));
    }

    public String asSimpleText() {
        return list.stream().map(Name::asSimpleText).collect(joining(",", "[", "]"));
    }

    public static Names empty() {
        return new Names(Collections.emptyList());
    }
}
