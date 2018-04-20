package jig.domain.model.report.validation;

import jig.application.service.GlossaryService;
import jig.domain.model.declaration.annotation.AnnotationDescription;
import jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;

public class AnnotationDetail {

    private final ValidationAnnotationDeclaration annotationDeclaration;
    private final GlossaryService glossaryService;
    private final TypeIdentifierFormatter typeIdentifierFormatter;

    public AnnotationDetail(ValidationAnnotationDeclaration annotationDeclaration, GlossaryService glossaryService, TypeIdentifierFormatter typeIdentifierFormatter) {
        this.annotationDeclaration = annotationDeclaration;
        this.glossaryService = glossaryService;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }

    public String declaringTypeName() {
        return annotationDeclaration.declaringType().format(typeIdentifierFormatter);
    }

    public JapaneseName japaneseName() {
        return glossaryService.japaneseNameFrom(annotationDeclaration.declaringType());
    }

    public String annotateSimpleName() {
        return annotationDeclaration.annotateSimpleName();
    }

    public TypeIdentifier annotationType() {
        return annotationDeclaration.annotationType();
    }

    public AnnotationDescription description() {
        return annotationDeclaration.annotationDescription();
    }
}
