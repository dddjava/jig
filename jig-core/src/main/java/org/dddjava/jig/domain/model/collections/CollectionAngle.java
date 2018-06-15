package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

public class CollectionAngle {

    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public CollectionAngle(TypeDependencies allTypeDependencies, TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = allTypeDependencies.stream()
                .filterTo(typeIdentifier)
                .removeSelf()
                .fromTypeIdentifiers();
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }

    public UserNumber userNumber() {
        return new UserNumber(userTypeIdentifiers().list().size());
    }
}
