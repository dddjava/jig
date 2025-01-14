package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;

/**
 * 使用フィールド一覧
 */
public class UsingFields {

    // FIXME 使用フィールドは型パラメタをもてないのでおそらくFieldDeclarationを使うのは不適切
    FieldDeclarations fieldDeclarations;

    public UsingFields(FieldDeclarations fieldDeclarations) {
        this.fieldDeclarations = fieldDeclarations;
    }

    public TypeIdentifiers typeIdentifiers() {
        return fieldDeclarations.toTypeIdentifies();
    }
}
