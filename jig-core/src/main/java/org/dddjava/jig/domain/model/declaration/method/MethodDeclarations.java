package org.dddjava.jig.domain.model.declaration.method;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MethodDeclarations {

    List<MethodDeclaration> list;

    public MethodDeclarations(List<MethodDeclaration> list) {
        this.list = list;
        list.sort(Comparator.comparing(MethodDeclaration::asFullText));
    }

    public List<MethodDeclaration> list() {
        return list;
    }

    public static Collector<MethodDeclaration, ?, MethodDeclarations> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), MethodDeclarations::new);
    }

    public MethodDeclarations filter(Predicate<MethodDeclaration> predicate) {
        return list.stream().filter(predicate).collect(collector());
    }

    public static MethodDeclarations empty() {
        return new MethodDeclarations(Collections.emptyList());
    }

    public MethodDeclarations map(Function<MethodDeclaration, MethodDeclarations> function) {
        if (list.isEmpty()) return MethodDeclarations.empty();
        // TODO 複数の場合
        return function.apply(list.get(0));
    }

    public String asSimpleText() {
        return list.stream().map(methodIdentifier ->
                methodIdentifier.declaringType().asSimpleText() + "." + methodIdentifier.asSimpleText()
        ).collect(Collectors.joining(", ", "[", "]"));
    }

    public MethodDeclarations distinct() {
        return list.stream().distinct().collect(MethodDeclarations.collector());
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.stream().anyMatch(methodDeclaration::equals);
    }
}
