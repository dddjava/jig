package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.List;

/**
 * 使用フィールド一覧
 */
public class UsingFields {

    List<FieldDeclaration> list;

    public UsingFields(List<FieldDeclaration> list) {
        this.list = list;
    }

    public UsingFields(FieldDeclarations usingFields) {
        this(usingFields.list());
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream().map(FieldDeclaration::typeIdentifier).collect(TypeIdentifiers.collector());
    }
}
