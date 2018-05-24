package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.stream.Stream;

public class MethodUsingFieldStream {

    Stream<MethodUsingField> stream;

    public MethodUsingFieldStream(Stream<MethodUsingField> stream) {
        this.stream = stream;
    }

    public MethodUsingFieldStream filter(MethodDeclaration methodDeclaration) {
        return new MethodUsingFieldStream(stream.filter(methodUsingField -> methodUsingField.userIs(methodDeclaration)));
    }

    public FieldDeclarations fields() {
        return stream.map(MethodUsingField::field).collect(FieldDeclarations.collector());
    }
}
