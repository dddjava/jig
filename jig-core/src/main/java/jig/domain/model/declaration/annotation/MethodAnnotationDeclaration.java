package jig.domain.model.declaration.annotation;

import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

public class MethodAnnotationDeclaration {

    final MethodDeclaration methodDeclaration;
    final TypeIdentifier annotationType;

    public MethodAnnotationDeclaration(MethodDeclaration methodDeclaration, TypeIdentifier annotationType) {
        this.methodDeclaration = methodDeclaration;
        this.annotationType = annotationType;
    }

    public TypeIdentifier annotationType() {
        return annotationType;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }
}
