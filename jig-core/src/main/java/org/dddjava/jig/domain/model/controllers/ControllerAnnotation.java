package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;

import java.util.List;

public class ControllerAnnotation {

    private final TypeAnnotations typeAnnotations;
    private final List<MethodAnnotation> methodAnnotations;

    public ControllerAnnotation(TypeAnnotations typeAnnotations, List<MethodAnnotation> methodAnnotations) {
        this.typeAnnotations = typeAnnotations;
        this.methodAnnotations = methodAnnotations;
    }
}
