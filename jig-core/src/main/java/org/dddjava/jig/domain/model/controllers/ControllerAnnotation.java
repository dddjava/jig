package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * コントローラーアノテーション
 */
public class ControllerAnnotation {

    private final Annotations typeAnnotations;
    private final Annotations methodAnnotations;

    public ControllerAnnotation(TypeAnnotations typeAnnotations, MethodAnnotations methodAnnotations) {
        TypeIdentifier[] mappingAnnotations = {
                new TypeIdentifier("org.springframework.web.bind.annotation.RequestMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.GetMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.PostMapping")
        };
        this.typeAnnotations = typeAnnotations.annotations().filterAny(mappingAnnotations);
        this.methodAnnotations = methodAnnotations.annotations().filterAny(mappingAnnotations);
    }

    public String typePathText() {
        return typeAnnotations.descriptionTextsOf("value").toString();
    }

    public String methodPathText() {
        return methodAnnotations.descriptionTextsOf("value").toString();
    }
}
