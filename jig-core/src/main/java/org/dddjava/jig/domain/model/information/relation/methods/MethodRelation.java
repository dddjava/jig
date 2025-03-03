package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッドの使用しているメソッド
 */
public record MethodRelation(JigMethodIdentifier from, JigMethodIdentifier to) {

    public boolean calleeMethodIs(JigMethodIdentifier jigMethodIdentifier) {
        return to().equals(jigMethodIdentifier);
    }

    public TypeIdentifier toType() {
        return to().tuple().declaringTypeIdentifier();
    }

    public TypeIdentifier fromType() {
        return from().tuple().declaringTypeIdentifier();
    }
}
