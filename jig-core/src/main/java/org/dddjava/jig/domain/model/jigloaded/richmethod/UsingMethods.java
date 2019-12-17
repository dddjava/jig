package org.dddjava.jig.domain.model.jigloaded.richmethod;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodReturn;

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
