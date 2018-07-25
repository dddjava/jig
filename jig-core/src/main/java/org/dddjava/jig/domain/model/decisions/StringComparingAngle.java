package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

public class StringComparingAngle {

    MethodDeclaration methodDeclaration;

    public StringComparingAngle(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }
}
