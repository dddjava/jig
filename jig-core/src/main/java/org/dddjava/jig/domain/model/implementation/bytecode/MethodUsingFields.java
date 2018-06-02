package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

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

    public MethodUsingFields(ByteCodes byteCodes) {
        this(new ArrayList<>());

        for (ByteCode byteCode : byteCodes.list()) {
            for (MethodByteCode methodByteCode : byteCode.instanceMethodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (FieldDeclaration usingField : methodByteCode.usingFields().list()) {
                    list.add(new MethodUsingField(methodDeclaration, usingField));
                }
            }
        }
    }

    public MethodUsingFieldStream stream() {
        return new MethodUsingFieldStream(list.stream());
    }
}
