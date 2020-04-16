package org.dddjava.jig.domain.model.jigloaded.relation.method;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * メソッドの使用しているメソッド一覧
 */
public class MethodRelations {

    private final List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    public CallerMethods callerMethodsOf(CalleeMethod calleeMethod) {
        List<CallerMethod> callers = list.stream()
                .filter(methodRelation -> methodRelation.calleeMethodIs(calleeMethod))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }
}
