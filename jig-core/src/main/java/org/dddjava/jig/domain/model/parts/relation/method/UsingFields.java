package org.dddjava.jig.domain.model.parts.relation.method;

import org.dddjava.jig.domain.model.parts.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifiers;

/**
 * 使用フィールド一覧
 */
public class UsingFields {

    FieldDeclarations fieldDeclarations;

    public UsingFields(FieldDeclarations fieldDeclarations) {
        this.fieldDeclarations = fieldDeclarations;
    }

    public TypeIdentifiers typeIdentifiers() {
        return fieldDeclarations.toTypeIdentifies();
    }
}
