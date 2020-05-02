package org.dddjava.jig.domain.model.jigloaded.richmethod;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

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

    public boolean empty() {
        return fieldDeclarations.empty();
    }
}
