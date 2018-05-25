package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedMethods {
    private final Implementations implementations;

    public AnnotatedMethods(Implementations implementations) {
        this.implementations = implementations;
    }

    public List<FieldAnnotationDeclaration> list() {
        List<FieldAnnotationDeclaration> fieldAnnotationDeclarations = new ArrayList<>();
        for (Implementation implementation : implementations.list()) {
            fieldAnnotationDeclarations.addAll(implementation.fieldAnnotationDeclarations());
        }
        return fieldAnnotationDeclarations;
    }
}
