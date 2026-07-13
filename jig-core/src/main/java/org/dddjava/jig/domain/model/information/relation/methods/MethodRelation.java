package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * メソッドの関連
 */
public record MethodRelation(JigMethodId from, JigMethodId to) {

    public static MethodRelation from(JigMethodId from, JigMethodId to) {
        return new MethodRelation(from, to);
    }

    public TypeId toType() {
        return to().tuple().declaringTypeId();
    }

    public TypeId fromType() {
        return from().tuple().declaringTypeId();
    }
}
