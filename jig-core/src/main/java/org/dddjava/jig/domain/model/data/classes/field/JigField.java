package org.dddjava.jig.domain.model.data.classes.field;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotations;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

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

    public static JigField from(JigFieldHeader jigFieldHeader) {
        var fieldDeclaration = new FieldDeclaration(
                jigFieldHeader.id().declaringTypeIdentifier(),
                new FieldType(jigFieldHeader.jigFieldAttribute().typeReference().id()),
                jigFieldHeader.id().name()
        );
        return new JigField(
                fieldDeclaration,
                new FieldAnnotations(
                        jigFieldHeader.jigFieldAttribute().declarationAnnotations().stream()
                                .map(jigAnnotationReference -> {
                                    var description = new AnnotationDescription();
                                    jigAnnotationReference.elements().forEach(element -> description.addParam(element.name(), element.value()));
                                    return new FieldAnnotation(
                                            new Annotation(
                                                    jigAnnotationReference.id(),
                                                    description
                                            ),
                                            fieldDeclaration
                                    );
                                })
                                .toList()));
    }

    public boolean matches(FieldDeclaration fieldDeclaration) {
        return this.fieldDeclaration.matches(fieldDeclaration);
    }

    public JigField newInstanceWith(FieldAnnotation fieldAnnotation) {
        ArrayList<FieldAnnotation> fieldAnnotations = new ArrayList<>(this.fieldAnnotations.list());
        fieldAnnotations.add(fieldAnnotation);
        return new JigField(fieldDeclaration, new FieldAnnotations(fieldAnnotations));
    }

    public TypeIdentifier typeIdentifier() {
        return fieldDeclaration.typeIdentifier();
    }

    public FieldDeclaration fieldDeclaration() {
        return fieldDeclaration;
    }

    public FieldAnnotations fieldAnnotations() {
        return fieldAnnotations;
    }

    public String nameText() {
        return fieldDeclaration.nameText();
    }

    public boolean isDeprecated() {
        return hasAnnotation(TypeIdentifier.from(Deprecated.class));
    }

    private boolean hasAnnotation(TypeIdentifier typeIdentifier) {
        for (FieldAnnotation annotation : fieldAnnotations.list()) {
            if (annotation.annotationType().equals(typeIdentifier)) {
                return true;
            }
        }
        return false;
    }
}
