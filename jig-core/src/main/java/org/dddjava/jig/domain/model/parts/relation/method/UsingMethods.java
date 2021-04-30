package org.dddjava.jig.domain.model.parts.relation.method;

import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.class_.method.MethodReturn;

/**
 * 使用しているメソッド一覧
 */
public class UsingMethods {
    MethodDeclarations list;

    public UsingMethods(MethodDeclarations list) {
        this.list = list;
    }

    public boolean containsStream() {
        return list.list().stream()
                .map(MethodDeclaration::methodReturn)
                .anyMatch(MethodReturn::isStream);
    }

    public MethodDeclarations methodDeclarations() {
        return list;
    }
}
