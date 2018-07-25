package org.dddjava.jig.domain.model.smells;

import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;

public class MethodSmellAngle {

    Method method;
    MethodUsingFields methodUsingFields;

    public MethodSmellAngle(Method method, MethodUsingFields methodUsingFields) {
        this.method = method;
        this.methodUsingFields = methodUsingFields;
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
        return methodUsingFields.usingFieldTypeIdentifiers(method.declaration()).empty();
    }

    public boolean primitiveInterface() {
        return method.declaration().returnType().isPrimitive()
                || method.declaration().methodSignature().arguments().stream().anyMatch(TypeIdentifier::isPrimitive);
    }

    public boolean hasSmell() {
        return notUseField() || primitiveInterface() || method.decisionNumber().notZero();
    }
}
