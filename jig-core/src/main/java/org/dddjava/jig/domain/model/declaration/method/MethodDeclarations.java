package org.dddjava.jig.domain.model.declaration.method;

import java.util.Comparator;
import java.util.List;
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

    public String asSimpleText() {
        return list.stream()
                .map(methodIdentifier -> methodIdentifier.declaringType().asSimpleText() + "." + methodIdentifier.asSimpleText())
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.stream().anyMatch(methodDeclaration::equals);
    }
}
