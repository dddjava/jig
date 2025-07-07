package org.dddjava.jig.domain.model.data.members.fields;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * フィールドのID
 *
 * `{クラスの完全修飾名}#{フィールド名}`
 */
public record JigFieldId(String value) {

    public static JigFieldId from(TypeId declaringTypeId, String name) {
        return new JigFieldId("%s#%s".formatted(declaringTypeId.fullQualifiedName(), name));
    }

    public String name() {
        return value.split("#")[1];
    }

    public TypeId declaringTypeIdentifier() {
        return TypeId.valueOf(value.split("#")[0]);
    }

    public String fqn() {
        return value;
    }
}
