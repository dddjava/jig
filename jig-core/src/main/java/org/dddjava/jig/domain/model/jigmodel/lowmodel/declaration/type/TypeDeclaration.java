package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type;

/**
 * 型宣言
 */
public class TypeDeclaration {

    ParameterizedType parameterizedType;
    ParameterizedType superType;
    ParameterizedTypes interfaceTypes;

    public TypeDeclaration(ParameterizedType parameterizedType, ParameterizedType superType, ParameterizedTypes interfaceTypes) {
        this.parameterizedType = parameterizedType;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
    }

    public TypeIdentifier identifier() {
        return parameterizedType.typeIdentifier();
    }

    public ParameterizedType superType() {
        return superType;
    }

    public ParameterizedTypes interfaceTypes() {
        return interfaceTypes;
    }
}
