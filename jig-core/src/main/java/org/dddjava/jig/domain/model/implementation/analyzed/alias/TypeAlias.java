package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * 型別名
 */
public class TypeAlias {
    TypeIdentifier typeIdentifier;
    Alias alias;

    public TypeAlias(TypeIdentifier typeIdentifier, Alias alias) {
        this.typeIdentifier = typeIdentifier;
        this.alias = alias;
    }

    public static TypeAlias empty(TypeIdentifier typeIdentifier) {
        return new TypeAlias(typeIdentifier, Alias.empty());
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public boolean exists() {
        return alias.exists();
    }

    public String asText() {
        return alias.toString();
    }
}
