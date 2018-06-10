package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

/**
 * 値の切り口
 */
public class ValueAngle {

    ValueKind valueKind;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public ValueAngle(ValueKind valueKind, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers) {
        this.valueKind = valueKind;
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = userTypeIdentifiers;
    }

    public static ValueAngle of(ValueKind valueKind, TypeDependencies allTypeDependencies, TypeIdentifier typeIdentifier) {
        TypeIdentifiers userTypeIdentifiers = allTypeDependencies.stream()
                .filterTo(typeIdentifier)
                .removeSelf()
                .fromTypeIdentifiers();
        return new ValueAngle(valueKind, typeIdentifier, userTypeIdentifiers);
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
