package jig.domain.model.report.validation;

import jig.application.service.GlossaryService;
import jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;

public class AnnotationDetail {

    private FieldAnnotationDeclaration fieldAnnotationDeclaration;
    private GlossaryService glossaryService;

    public AnnotationDetail(FieldAnnotationDeclaration fieldAnnotationDeclaration, GlossaryService glossaryService) {
        this.fieldAnnotationDeclaration = fieldAnnotationDeclaration;
        this.glossaryService = glossaryService;
    }

    public TypeIdentifier declaringType() {
        return fieldAnnotationDeclaration.fieldDeclaration().declaringType();
    }

    public JapaneseName japaneseName() {
        return glossaryService.japaneseNameFrom(declaringType());
    }

    public String annotateSimpleName() {
        return fieldAnnotationDeclaration.fieldDeclaration().nameText();
    }

    public TypeIdentifier annotationType() {
        return fieldAnnotationDeclaration.annotationType();
    }
}
