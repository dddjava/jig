package org.dddjava.jig.domain.model.data.classes.field;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotations;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

public class JigField {
    private final JigFieldHeader jigFieldHeader;
    FieldDeclaration fieldDeclaration;
    FieldAnnotations fieldAnnotations;

    public JigField(JigFieldHeader jigFieldHeader, FieldDeclaration fieldDeclaration, FieldAnnotations fieldAnnotations) {
        this.jigFieldHeader = jigFieldHeader;
        this.fieldDeclaration = fieldDeclaration;
        this.fieldAnnotations = fieldAnnotations;
    }

    public JigField(FieldDeclaration fieldDeclaration) {
        this(null, fieldDeclaration, FieldAnnotations.none());
    }

    public JigTypeReference jigTypeReference() {
        return jigFieldHeader.jigTypeReference();
    }

    public static JigField from(JigFieldHeader jigFieldHeader) {
        // 互換のため無理矢理JigFieldHeaderから生成している状態。FieldDeclarationやFieldAnnotationsの使用箇所を直せばもっと素直にできるはず
        var fieldDeclaration = new FieldDeclaration(
                jigFieldHeader.id().declaringTypeIdentifier(),
                new FieldType(jigFieldHeader.jigTypeReference().id()),
                jigFieldHeader.id().name()
        );
        return new JigField(
                jigFieldHeader,
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
        return jigFieldHeader.isDeprecated();
    }
}
