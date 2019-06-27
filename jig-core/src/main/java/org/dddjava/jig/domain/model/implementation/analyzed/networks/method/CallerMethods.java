package org.dddjava.jig.domain.model.implementation.analyzed.networks.method;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.List;

/**
 * 呼び出しメソッド一覧
 */
public class CallerMethods {
    List<MethodDeclaration> list;

    public CallerMethods(List<MethodDeclaration> list) {
        this.list = list;
    }

    public UserNumber toUserNumber() {
        return new UserNumber(list.size());
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.stream().anyMatch(methodDeclaration::sameIdentifier);
    }

    public List<MethodDeclaration> list() {
        return list;
    }
}
