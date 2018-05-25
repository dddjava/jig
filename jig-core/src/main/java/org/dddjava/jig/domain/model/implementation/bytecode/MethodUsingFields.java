package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class MethodUsingFields {

    List<MethodUsingField> list;

    public MethodUsingFields(List<MethodUsingField> list) {
        this.list = list;
    }

    public MethodUsingFields(Implementations implementations) {
        this(new ArrayList<>());

        for (Implementation implementation : implementations.list()) {
            for (MethodImplementation methodSpecification : implementation.instanceMethodSpecifications()) {
                MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;
                for (FieldDeclaration usingField : methodSpecification.usingFields().list()) {
                    list.add(new MethodUsingField(methodDeclaration, usingField));
                }
            }
        }
    }

    public MethodUsingFieldStream stream() {
        return new MethodUsingFieldStream(list.stream());
    }
}
