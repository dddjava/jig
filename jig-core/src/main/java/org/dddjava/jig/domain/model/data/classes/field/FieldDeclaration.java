package org.dddjava.jig.domain.model.data.classes.field;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.JigObjectId;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;

/**
 * フィールド定義
 */
public class FieldDeclaration {

    TypeIdentifier declaringType;
    FieldType fieldType;
    String name;

    public FieldDeclaration(TypeIdentifier declaringType, FieldType fieldType, String name) {
        this.declaringType = declaringType;
        this.name = name;
        this.fieldType = fieldType;
    }

    public FieldDeclaration(JigObjectId<JigTypeHeader> jigTypeId, FieldType fieldType, String name) {
        this.declaringType = TypeIdentifier.valueOf(jigTypeId.value());
        this.name = name;
        this.fieldType = fieldType;
    }

    public TypeIdentifier typeIdentifier() {
        return fieldType.nonGenericTypeIdentifier();
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

    public FieldType fieldType() {
        return fieldType;
    }

    public boolean matches(FieldDeclaration other) {
        return this.declaringType.equals(other.declaringType)
                && this.typeIdentifier().equals(other.typeIdentifier())
                && this.name.equals(other.name);
    }
}
