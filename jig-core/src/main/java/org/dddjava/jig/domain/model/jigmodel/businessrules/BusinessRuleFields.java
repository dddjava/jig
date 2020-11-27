package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;

/**
 * ビジネスルールのフィールド
 */
public class BusinessRuleFields {
    FieldDeclarations fieldDeclarations;

    public BusinessRuleFields(FieldDeclarations fieldDeclarations) {
        this.fieldDeclarations = fieldDeclarations;
    }

    public FieldType onlyOneFieldType() {
        return fieldDeclarations.onlyOneField().fieldType();
    }

    public FieldDeclarations fieldDeclarations() {
        return fieldDeclarations;
    }

    public TypeIdentifiers typeIdentifiers() {
        return fieldDeclarations.toTypeIdentifies();
    }
}
