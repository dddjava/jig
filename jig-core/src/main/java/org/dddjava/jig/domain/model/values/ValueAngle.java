package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.networks.type.TypeRelations;

/**
 * 値の切り口
 */
public class ValueAngle {

    ValueKind valueKind;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public ValueAngle(ValueKind valueKind, TypeRelations allTypeRelations, ValueType valueType) {
        this.valueKind = valueKind;
        this.typeIdentifier = valueType.typeIdentifier();
        this.userTypeIdentifiers = allTypeRelations.collectTypeIdentifierWhichRelationTo(typeIdentifier);
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }
}
