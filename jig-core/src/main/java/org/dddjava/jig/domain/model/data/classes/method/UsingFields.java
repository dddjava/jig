package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;

/**
 * 使用フィールド一覧
 */
public class UsingFields {

    // FIXME 使用フィールドは型パラメタをもてないのでおそらくFieldDeclarationを使うのは不適切
    FieldDeclarations fieldDeclarations;

    public UsingFields(FieldDeclarations fieldDeclarations) {
        this.fieldDeclarations = fieldDeclarations;
    }

    public String typeNames() {
        return fieldDeclarations.toTypeIdentifies().asSimpleText();
    }
}
