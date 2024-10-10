package org.dddjava.jig.domain.model.parts.classes.method;

/**
 * メソッドの使用しているメソッド
 */
public record MethodRelation(MethodDeclaration from, MethodDeclaration to) {

    public boolean calleeMethodIs(MethodDeclaration calleeMethod) {
        return to.sameIdentifier(calleeMethod);
    }
}
