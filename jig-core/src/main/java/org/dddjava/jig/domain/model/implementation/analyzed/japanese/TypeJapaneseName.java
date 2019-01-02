package org.dddjava.jig.domain.model.implementation.analyzed.japanese;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * 型和名
 */
public class TypeJapaneseName {
    TypeIdentifier typeIdentifier;
    JapaneseName japaneseName;

    public TypeJapaneseName(TypeIdentifier typeIdentifier, JapaneseName japaneseName) {
        this.typeIdentifier = typeIdentifier;
        this.japaneseName = japaneseName;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public JapaneseName japaneseName() {
        return japaneseName;
    }

    public boolean exists() {
        return japaneseName.exists();
    }
}
