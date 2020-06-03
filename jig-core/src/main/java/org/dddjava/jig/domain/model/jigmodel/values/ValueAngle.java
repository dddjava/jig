package org.dddjava.jig.domain.model.jigmodel.values;

import org.dddjava.jig.domain.model.jigmodel.businessrules.ValueKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;

/**
 * 値の切り口
 */
public class ValueAngle {

    ValueKind valueKind;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public ValueAngle(ValueKind valueKind, ClassRelations allClassRelations, ValueType valueType) {
        this.valueKind = valueKind;
        this.typeIdentifier = valueType.typeIdentifier();
        this.userTypeIdentifiers = allClassRelations.collectTypeIdentifierWhichRelationTo(typeIdentifier);
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }
}
