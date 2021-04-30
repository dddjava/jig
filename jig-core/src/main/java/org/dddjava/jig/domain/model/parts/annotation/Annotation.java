package org.dddjava.jig.domain.model.parts.annotation;

import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

/**
 * アノテーション
 */
public class Annotation {

    final TypeIdentifier typeIdentifier;
    final AnnotationDescription description;

    public Annotation(TypeIdentifier typeIdentifier, AnnotationDescription description) {
        this.typeIdentifier = typeIdentifier;
        this.description = description;
    }

    public String descriptionTextOf(String name) {
        return description.textOf(name);
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public boolean is(TypeIdentifier typeIdentifier) {
        return this.typeIdentifier.equals(typeIdentifier);
    }
}
