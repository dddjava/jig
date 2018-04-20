package jig.domain.model.definition.method;

import jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MethodDefinitions {

    List<MethodDefinition> list;

    public MethodDefinitions(List<MethodDefinition> list) {
        this.list = list;
        list.sort(Comparator.comparing(MethodDefinition::asFullText));
    }

    public List<MethodDefinition> list() {
        return list;
    }

    public static Collector<MethodDefinition, ?, MethodDefinitions> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), MethodDefinitions::new);
    }

    public MethodDefinitions filter(Predicate<MethodDefinition> predicate) {
        return list.stream().filter(predicate).collect(collector());
    }

    public static MethodDefinitions empty() {
        return new MethodDefinitions(Collections.emptyList());
    }

    public MethodDefinitions map(Function<MethodDefinition, MethodDefinitions> function) {
        if (list.isEmpty()) return MethodDefinitions.empty();
        // TODO 複数の場合
        return function.apply(list.get(0));
    }

    public TypeIdentifiers declaringTypes() {
        return list.stream().map(MethodDefinition::declaringType).collect(TypeIdentifiers.collector());
    }

    public String asSimpleText() {
        return list.stream().map(methodIdentifier ->
                methodIdentifier.declaringType().asSimpleText() + "." + methodIdentifier.asSimpleText()
        ).collect(Collectors.joining(","));
    }
}
