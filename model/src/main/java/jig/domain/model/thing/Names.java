package jig.domain.model.thing;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
}
