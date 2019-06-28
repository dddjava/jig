package org.dddjava.jig.domain.model.declaration.field;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * フィールド定義
 */
public class FieldDeclaration {

    TypeIdentifier declaringType;
    String name;
    FieldType fieldType;

    public FieldDeclaration(TypeIdentifier declaringType, String name, FieldType fieldType) {
        this.declaringType = declaringType;
        this.name = name;
        this.fieldType = fieldType;
    }

    public TypeIdentifier typeIdentifier() {
        return fieldType.typeIdentifier;
    }

    public String nameText() {
        return name;
    }

    public String signatureText() {
        return String.format("%s %s", typeIdentifier().asSimpleText(), name);
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }
}
