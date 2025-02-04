package org.dddjava.jig.domain.model.data.classes.annotation;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Optional;

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

    public AnnotationDescription description() {
        return description;
    }

    public boolean is(TypeIdentifier typeIdentifier) {
        return this.typeIdentifier.equals(typeIdentifier);
    }

    public Optional<String> descriptionTextAnyOf(String... names) {
        return description.textAnyOf(names);
    }
}
