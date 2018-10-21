package org.dddjava.jig.domain.model.networks.method;

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

    public boolean toIs(MethodIdentifier methodIdentifier) {
        return to.identifier().equals(methodIdentifier);
    }

    public boolean toIs(MethodDeclaration methodDeclaration) {
        return to.sameIdentifier(methodDeclaration);
    }
}
