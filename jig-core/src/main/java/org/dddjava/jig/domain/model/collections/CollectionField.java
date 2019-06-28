package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldType;

public class CollectionField {
    FieldDeclaration fieldDeclaration;

    public CollectionField(FieldDeclaration fieldDeclaration) {
        this.fieldDeclaration = fieldDeclaration;
    }

    public FieldType fieldType() {
        return fieldDeclaration.fieldType();
    }
}
