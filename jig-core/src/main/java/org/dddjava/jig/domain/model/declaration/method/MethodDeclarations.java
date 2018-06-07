package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.basic.Text;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
        return Collectors.collectingAndThen(toList(), MethodDeclarations::new);
    }

    public String asSimpleText() {
        return list.stream()
                .map(MethodDeclaration::asSimpleTextWithDeclaringType)
                .sorted()
                .collect(Text.collectionCollector());
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.contains(methodDeclaration);
    }

    public TypeIdentifiers declaringTypes() {
        return new TypeIdentifiers(
                list.stream()
                        .map(MethodDeclaration::declaringType)
                        .distinct()
                        .collect(toList())
        );
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
