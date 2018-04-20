package jig.domain.model.declaration.annotation;

import java.util.List;

public interface AnnotationDeclarationRepository {

    List<ValidationAnnotationDeclaration> findValidationAnnotation();

    void register(FieldAnnotationDeclaration fieldAnnotationDeclaration);

    void register(MethodAnnotationDeclaration methodAnnotationDeclaration);
}
