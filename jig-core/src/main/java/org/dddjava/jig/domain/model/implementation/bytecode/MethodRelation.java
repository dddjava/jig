package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

/**
 * メソッドの使用しているメソッド
 */
public class MethodRelation {
    MethodDeclaration from;
    MethodDeclaration to;

    public MethodRelation(MethodDeclaration from, MethodDeclaration to) {
        this.from = from;
        this.to = to;
    }

    MethodDeclaration from() {
        return from;
    }

    MethodDeclaration to() {
        return to;
    }

    public boolean toIs(MethodDeclaration methodDeclaration) {
        return to.sameIdentifier(methodDeclaration);
    }

    public boolean fromIs(MethodDeclaration methodDeclaration) {
        return from.sameIdentifier(methodDeclaration);
    }
}
