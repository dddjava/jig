package org.dddjava.jig.domain.model.declaration.type;

/**
 * 型
 */
public class Type {

    TypeIdentifier typeIdentifier;
    ParameterizedType superType;

    public Type(TypeIdentifier typeIdentifier, ParameterizedType superType) {
        this.typeIdentifier = typeIdentifier;
        this.superType = superType;
    }

    public TypeIdentifier identifier() {
        return typeIdentifier;
    }

    public ParameterizedType superType() {
        return superType;
    }
}
