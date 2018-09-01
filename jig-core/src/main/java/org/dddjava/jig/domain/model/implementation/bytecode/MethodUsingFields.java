package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.List;

/**
 * メソッドが使用しているフィールド一覧
 */
public class MethodUsingFields {

    List<MethodUsingField> list;

    public MethodUsingFields(List<MethodUsingField> list) {
        this.list = list;
    }

    private MethodUsingFieldStream stream() {
        return new MethodUsingFieldStream(list.stream());
    }

    public TypeIdentifiers usingFieldTypeIdentifiers(MethodDeclaration method) {
        return stream()
                .filter(method)
                .fields()
                .toTypeIdentifies();
    }
}
