package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * メソッドが使用しているフィールド一覧
 */
public class MethodUsingFields {

    List<MethodUsingField> list;

    public MethodUsingFields(List<MethodUsingField> list) {
        this.list = list;
    }

    public MethodUsingFields(TypeByteCodes typeByteCodes) {
        this(new ArrayList<>());

        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            for (MethodByteCode methodByteCode : typeByteCode.instanceMethodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (FieldDeclaration usingField : methodByteCode.usingFields().list()) {
                    list.add(new MethodUsingField(methodDeclaration, usingField));
                }
            }
        }
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
