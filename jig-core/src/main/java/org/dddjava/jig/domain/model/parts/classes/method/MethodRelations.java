package org.dddjava.jig.domain.model.parts.classes.method;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * メソッドの使用しているメソッド一覧
 */
public class MethodRelations {

    private final List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    /**
     * 呼び出し元メソッドのフィルタリング
     */
    public CallerMethods callerMethodsOf(MethodDeclaration calleeMethod) {
        List<MethodDeclaration> callers = list.stream()
                .filter(methodRelation -> methodRelation.calleeMethodIs(calleeMethod))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }

    public String mermaidEdgeText() {
        return list.stream()
                .map(MethodRelation::mermaidEdgeText)
                .collect(Collectors.joining("\n"));
    }

    public MethodRelations filterFromRecursive(MethodDeclaration methodDeclaration) {
        var stopper = new HashSet<MethodIdentifier>();

        return filterFromRecursiveInternal(methodDeclaration, stopper)
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }

    private Stream<MethodRelation> filterFromRecursiveInternal(MethodDeclaration baseMethod, Set<MethodIdentifier> stopper) {
        if (stopper.contains(baseMethod.identifier())) return Stream.empty();

        return list.stream()
                .filter(methodRelation -> methodRelation.from().sameIdentifier(baseMethod))
                .flatMap(methodRelation -> Stream.concat(
                        Stream.of(methodRelation),
                        filterFromRecursiveInternal(methodRelation.to(), stopper)));
    }

    public Set<MethodIdentifier> methodIdentifiers() {
        return list.stream()
                .flatMap(methodRelation -> Stream.of(methodRelation.from(), methodRelation.to()))
                .map(MethodDeclaration::identifier)
                .collect(Collectors.toSet());
    }

    public MethodRelations filterTo(MethodDeclaration declaration) {
        return list.stream()
                .filter(methodRelation -> methodRelation.to().sameIdentifier(declaration))
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }

    public MethodRelations merge(MethodRelations others) {
        return Stream.concat(list.stream(), others.list.stream())
                .collect(collectingAndThen(toList(), MethodRelations::new));
    }
}
