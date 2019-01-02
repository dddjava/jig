package org.dddjava.jig.domain.model.implementation.analyzed.declaration.type;

/**
 * 型パラメーター
 *
 * 現在は境界型などは扱わない。
 */
public class TypeParameter {

    TypeIdentifier typeIdentifier;

    public TypeParameter(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }
}
