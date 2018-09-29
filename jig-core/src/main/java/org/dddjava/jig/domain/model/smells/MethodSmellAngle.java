package org.dddjava.jig.domain.model.smells;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.unit.method.Method;

/**
 * メソッドの不吉なにおい
 */
public class MethodSmellAngle {

    Method method;
    MethodUsingFields methodUsingFields;
    FieldDeclarations fieldDeclarations;

    public MethodSmellAngle(Method method, MethodUsingFields methodUsingFields, FieldDeclarations fieldDeclarations) {
        this.method = method;
        this.methodUsingFields = methodUsingFields;
        this.fieldDeclarations = fieldDeclarations;
    }

    public TypeIdentifier typeIdentifier() {
        return methodDeclaration().declaringType();
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public String decisionNumber() {
        return method.decisionNumber().asText();
    }

    public boolean notUseField() {
        return !fieldDeclarations.list().isEmpty() &&
                methodUsingFields.usingFieldTypeIdentifiers(method.declaration()).empty();
    }

    public boolean primitiveInterface() {
        return method.declaration().returnType().isPrimitive()
                || method.declaration().methodSignature().arguments().stream().anyMatch(TypeIdentifier::isPrimitive);
    }

    public boolean returnsBoolean() {
        return method.declaration().methodReturn().parameterizedType().typeIdentifier().isBoolean();
    }

    public boolean hasSmell() {
        return notUseField() || primitiveInterface() || method.decisionNumber().notZero() || returnsBoolean();
    }
}
