package org.dddjava.jig.domain.model.parts.classes.method;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * メソッドの使用しているメソッド一覧
 */
public class MethodRelations {

    private final List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    public CallerMethods callerMethodsOf(MethodDeclaration calleeMethod) {
        List<MethodDeclaration> callers = list.stream()
                .filter(methodRelation -> methodRelation.calleeMethodIs(calleeMethod))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }

    public String mermaidEdgeText() {
        return list.stream()
                .map(e -> "%s --> %s".formatted(e.from().htmlIdText(), e.to().htmlIdText()))
                .collect(Collectors.joining("\n"));
    }
}
