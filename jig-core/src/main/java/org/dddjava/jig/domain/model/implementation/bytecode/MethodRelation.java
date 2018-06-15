package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;

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

    public boolean toIs(MethodIdentifier methodIdentifier) {
        return to.identifier().equals(methodIdentifier);
    }

    public boolean fromIs(MethodDeclaration methodDeclaration) {
        return from.sameIdentifier(methodDeclaration);
    }
}
