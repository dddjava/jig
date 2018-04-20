package jig.domain.model.report.validation;

import jig.application.service.GlossaryService;
import jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;

public class AnnotationDetail {

    private ValidationAnnotationDeclaration annotationDeclaration;
    private GlossaryService glossaryService;

    public AnnotationDetail(ValidationAnnotationDeclaration annotationDeclaration, GlossaryService glossaryService) {
        this.annotationDeclaration = annotationDeclaration;
        this.glossaryService = glossaryService;
    }

    public TypeIdentifier declaringType() {
        return annotationDeclaration.declaringType();
    }

    public JapaneseName japaneseName() {
        return glossaryService.japaneseNameFrom(declaringType());
    }

    public String annotateSimpleName() {
        return annotationDeclaration.annotateSimpleName();
    }

    public TypeIdentifier annotationType() {
        return annotationDeclaration.annotationType();
    }
}
