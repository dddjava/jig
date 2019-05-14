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

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public Alias japaneseName() {
        return alias;
    }

    public boolean exists() {
        return alias.exists();
    }
}
