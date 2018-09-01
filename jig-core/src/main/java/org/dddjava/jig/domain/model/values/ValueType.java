package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 値の型
 */
public class ValueType {
    private final TypeIdentifier typeIdentifier;
    private final FieldDeclarations fieldDeclarations;

    public ValueType(TypeIdentifier typeIdentifier, FieldDeclarations fieldDeclarations) {

        this.typeIdentifier = typeIdentifier;
        this.fieldDeclarations = fieldDeclarations;
    }

    public boolean is(ValueKind valueKind) {
        return valueKind.matches(fieldDeclarations);
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }
}
