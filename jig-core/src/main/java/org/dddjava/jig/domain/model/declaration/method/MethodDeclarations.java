package org.dddjava.jig.domain.model.declaration.method;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * メソッド定義一覧
 */
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

    public String asSimpleText() {
        return list.stream()
                .map(MethodDeclaration::asSimpleTextWithDeclaringType)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.contains(methodDeclaration);
    }
}
