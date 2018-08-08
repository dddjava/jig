package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

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
}
