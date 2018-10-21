package org.dddjava.jig.domain.model.angle.unit.method;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
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

    public TypeIdentifiers typeIdentifiers() {
        return list.stream().map(FieldDeclaration::typeIdentifier).collect(TypeIdentifiers.collector());
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
