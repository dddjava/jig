package org.dddjava.jig.domain.model.jigmodel.declaration.field;

import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;

/**
 * フィールド定義
 */
public class FieldDeclaration {

    TypeIdentifier declaringType;
    FieldIdentifier fieldIdentifier;
    FieldType fieldType;

    public FieldDeclaration(TypeIdentifier declaringType, FieldType fieldType, FieldIdentifier fieldIdentifier) {
        this.declaringType = declaringType;
        this.fieldIdentifier = fieldIdentifier;
        this.fieldType = fieldType;
    }

    public TypeIdentifier typeIdentifier() {
        return fieldType.nonGenericTypeIdentifier();
    }

    public String nameText() {
        return fieldIdentifier.text();
    }

    public String signatureText() {
        return String.format("%s %s", typeIdentifier().asSimpleText(), fieldIdentifier.text());
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public FieldType fieldType() {
        return fieldType;
    }
}
