package org.dddjava.jig.domain.model.parts.classes.type;

import java.util.ArrayList;
import java.util.List;

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

    public List<TypeIdentifier> listTypeIdentifiers() {
        List<TypeIdentifier> list = new ArrayList<>(parameterizedType.listTypeIdentifiers());
        list.addAll(superType.listTypeIdentifiers());
        list.addAll(interfaceTypes.listTypeIdentifiers());
        return list;
    }

    public ParameterizedTypes interfaceTypes() {
        return interfaceTypes;
    }

    public boolean extendsOrImplements(TypeIdentifier typeIdentifier) {
        return superType.typeIdentifier().equals(typeIdentifier) || interfaceTypes.listTypeIdentifiers().contains(typeIdentifier);
    }

    public boolean hasSuperOrInterface() {
        return !superType.typeIdentifier.equals(new TypeIdentifier(Object.class)) || interfaceTypes.exists();
    }

    public ParameterizedType superType() {
        return superType;
    }
}
