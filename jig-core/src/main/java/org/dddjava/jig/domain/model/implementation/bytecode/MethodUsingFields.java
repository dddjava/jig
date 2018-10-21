package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドが使用しているフィールド一覧
 */
public class MethodUsingFields {

    List<MethodUsingField> list;

    public MethodUsingFields(TypeByteCodes typeByteCodes) {
        this.list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            for (MethodByteCode methodByteCode : typeByteCode.instanceMethodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (FieldDeclaration usingField : methodByteCode.usingFields().list()) {
                    list.add(new MethodUsingField(methodDeclaration, usingField));
                }
            }
        }
    }

    public TypeIdentifiers usingFieldTypeIdentifiers(MethodDeclaration method) {
        return list.stream()
                .filter(methodUsingField -> methodUsingField.userIs(method))
                .map(MethodUsingField::field)
                .collect(FieldDeclarations.collector())
                .toTypeIdentifies();
    }

    public UsingFields usingFieldsOf(MethodDeclaration methodDeclaration) {
        List<FieldDeclaration> fields = list.stream().filter(methodUsingField -> methodUsingField.userIs(methodDeclaration))
                .map(MethodUsingField::field)
                .collect(Collectors.toList());
        return new UsingFields(fields);
    }
}
