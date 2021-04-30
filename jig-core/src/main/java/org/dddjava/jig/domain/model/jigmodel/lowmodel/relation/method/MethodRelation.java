package org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;

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

    public boolean calleeMethodIs(MethodDeclaration calleeMethod) {
        return to.sameIdentifier(calleeMethod);
    }
}
