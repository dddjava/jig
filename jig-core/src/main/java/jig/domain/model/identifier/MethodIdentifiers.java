package jig.domain.model.identifier;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MethodIdentifiers {

    List<MethodIdentifier> list;

    public MethodIdentifiers(List<MethodIdentifier> list) {
        this.list = list;
        list.sort(Comparator.comparing(MethodIdentifier::asFullText));
    }

    public List<MethodIdentifier> list() {
        return list;
    }

    public static Collector<MethodIdentifier, ?, MethodIdentifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), MethodIdentifiers::new);
    }

    public MethodIdentifiers filter(Predicate<MethodIdentifier> predicate) {
        return list.stream().filter(predicate).collect(collector());
    }

    public static MethodIdentifiers empty() {
        return new MethodIdentifiers(Collections.emptyList());
    }

    public MethodIdentifiers map(Function<MethodIdentifier, MethodIdentifiers> function) {
        if (list.isEmpty()) return MethodIdentifiers.empty();
        // TODO 複数の場合
        return function.apply(list.get(0));
    }

    public TypeIdentifiers declaringTypes() {
        return list.stream().map(MethodIdentifier::declaringType).collect(TypeIdentifiers.collector());
    }
}
