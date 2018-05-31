package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

/**
 * メソッドが使用しているフィールド
 */
public class MethodUsingField {

    MethodDeclaration methodDeclaration;
    FieldDeclaration fieldDeclaration;

    public MethodUsingField(MethodDeclaration methodDeclaration, FieldDeclaration fieldDeclaration) {
        this.methodDeclaration = methodDeclaration;
        this.fieldDeclaration = fieldDeclaration;
    }

    public boolean userIs(MethodDeclaration methodDeclaration) {
        return methodDeclaration.equals(this.methodDeclaration);
    }

    public FieldDeclaration field() {
        return fieldDeclaration;
    }
}
