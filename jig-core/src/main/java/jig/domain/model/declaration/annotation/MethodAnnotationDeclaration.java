package jig.domain.model.declaration.annotation;

import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

public class MethodAnnotationDeclaration {

    final MethodDeclaration methodDeclaration;
    final TypeIdentifier annotationType;
    final AnnotationDescription description;

    public MethodAnnotationDeclaration(MethodDeclaration methodDeclaration, TypeIdentifier annotationType, AnnotationDescription description) {
        this.methodDeclaration = methodDeclaration;
        this.annotationType = annotationType;
        this.description = description;
    }

    public TypeIdentifier annotationType() {
        return annotationType;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }

    public AnnotationDescription description() {
        return description;
    }
}
