package org.dddjava.jig.domain.model.jigmodel.declaration.type;

/**
 * 型
 */
public class Type {

    TypeIdentifier typeIdentifier;
    ParameterizedType superType;
    ParameterizedTypes interfaceTypes;

    public Type(TypeIdentifier typeIdentifier, ParameterizedType superType, ParameterizedTypes interfaceTypes) {
        this.typeIdentifier = typeIdentifier;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
    }

    public TypeIdentifier identifier() {
        return typeIdentifier;
    }

    public ParameterizedType superType() {
        return superType;
    }

    public ParameterizedTypes interfaceTypes() {
        return interfaceTypes;
    }
}
