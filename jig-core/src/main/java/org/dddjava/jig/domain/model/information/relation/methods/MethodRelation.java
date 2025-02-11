package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;

/**
 * メソッドの使用しているメソッド
 */
public record MethodRelation(MethodDeclaration from, MethodDeclaration to) {

    public String mermaidEdgeText() {
        return "%s --> %s".formatted(from().htmlIdText(), to().htmlIdText());
    }

    public boolean calleeMethodIs(JigMethodIdentifier jigMethodIdentifier) {
        return to.sameIdentifier(jigMethodIdentifier);
    }
}
