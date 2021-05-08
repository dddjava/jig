package org.dddjava.jig.domain.model.models.jigobject.member;

import org.dddjava.jig.domain.model.parts.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.parts.classes.annotation.FieldAnnotations;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;

import java.util.ArrayList;

public class JigField {
    FieldDeclaration fieldDeclaration;
    FieldAnnotations fieldAnnotations;

    public JigField(FieldDeclaration fieldDeclaration, FieldAnnotations fieldAnnotations) {
        this.fieldDeclaration = fieldDeclaration;
        this.fieldAnnotations = fieldAnnotations;
    }

    public JigField(FieldDeclaration fieldDeclaration) {
        this(fieldDeclaration, FieldAnnotations.none());
    }

    public boolean matches(FieldDeclaration fieldDeclaration) {
        return this.fieldDeclaration.matches(fieldDeclaration);
    }

    public JigField newInstanceWith(FieldAnnotation fieldAnnotation) {
        ArrayList<FieldAnnotation> fieldAnnotations = new ArrayList<>(this.fieldAnnotations.list());
        fieldAnnotations.add(fieldAnnotation);
        return new JigField(fieldDeclaration, new FieldAnnotations(fieldAnnotations));
    }

    public FieldDeclaration fieldDeclaration() {
        return fieldDeclaration;
    }

    public FieldAnnotations fieldAnnotations() {
        return fieldAnnotations;
    }
}
