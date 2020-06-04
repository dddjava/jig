package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

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
