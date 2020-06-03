package org.dddjava.jig.domain.model.jigmodel.relation.method;

import org.dddjava.jig.domain.model.jigmodel.declaration.method.MethodDeclaration;

import java.util.List;

/**
 * 呼び出しメソッド一覧
 */
public class CallerMethods {
    List<CallerMethod> list;

    public CallerMethods(List<CallerMethod> list) {
        this.list = list;
    }

    public UserNumber toUserNumber() {
        return new UserNumber(list.size());
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.stream()
                .anyMatch(callerMethod -> methodDeclaration.sameIdentifier(callerMethod.methodDeclaration));
    }

    public List<CallerMethod> list() {
        return list;
    }
}
