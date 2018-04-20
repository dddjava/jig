package jig.domain.model.declaration.annotation;

import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

public class MethodAnnotationDeclaration {

    final MethodDeclaration declaringMethod;
    final TypeIdentifier annotationType;

    public MethodAnnotationDeclaration(MethodDeclaration declaringMethod, TypeIdentifier annotationType) {
        this.declaringMethod = declaringMethod;
        this.annotationType = annotationType;
    }

    public TypeIdentifier annotationType() {
        return annotationType;
    }
}
