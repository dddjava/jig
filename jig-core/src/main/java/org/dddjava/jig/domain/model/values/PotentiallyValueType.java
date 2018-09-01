package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 値の可能性のある型
 */
public class PotentiallyValueType {

    TypeIdentifier typeIdentifier;
    FieldDeclarations fieldDeclarations;

    public PotentiallyValueType(TypeIdentifier typeIdentifier, FieldDeclarations fieldDeclarations) {
        this.typeIdentifier = typeIdentifier;
        this.fieldDeclarations = fieldDeclarations;
    }

    public ValueType toValueType() {
        return new ValueType(typeIdentifier, fieldDeclarations);
    }
}
