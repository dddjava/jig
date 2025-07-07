package org.dddjava.jig.domain.model.data.members.fields;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * フィールドのID
 *
 * `{クラスの完全修飾名}#{フィールド名}`
 */
public record JigFieldId(String value) {

    public static JigFieldId from(TypeIdentifier declaringTypeIdentifier, String name) {
        return new JigFieldId("%s#%s".formatted(declaringTypeIdentifier.fullQualifiedName(), name));
    }

    public String name() {
        return value.split("#")[1];
    }

    public TypeIdentifier declaringTypeIdentifier() {
        return TypeIdentifier.valueOf(value.split("#")[0]);
    }

    public String fqn() {
        return value;
    }
}
