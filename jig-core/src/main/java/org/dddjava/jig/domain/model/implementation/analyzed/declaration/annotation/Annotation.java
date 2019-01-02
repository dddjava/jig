package org.dddjava.jig.domain.model.implementation.analyzed.declaration.annotation;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * アノテーション
 */
public class Annotation {

    final TypeIdentifier annotationType;
    final AnnotationDescription description;

    public Annotation(TypeIdentifier annotationType, AnnotationDescription description) {
        this.annotationType = annotationType;
        this.description = description;
    }

    public String descriptionTextOf(String name) {
        return description.textOf(name);
    }

    public TypeIdentifier identifier() {
        return annotationType;
    }
}
