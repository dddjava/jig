package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedFields {
    private final Implementations implementations;

    public AnnotatedFields(Implementations implementations) {
        this.implementations = implementations;
    }

    public List<MethodAnnotationDeclaration> list() {
        List<MethodAnnotationDeclaration> methodAnnotationDeclarations = new ArrayList<>();
        for (MethodImplementation methodSpecification : implementations.instanceMethodSpecifications()) {
            methodAnnotationDeclarations.addAll(methodSpecification.methodAnnotationDeclarations());
        }
        return methodAnnotationDeclarations;
    }
}
