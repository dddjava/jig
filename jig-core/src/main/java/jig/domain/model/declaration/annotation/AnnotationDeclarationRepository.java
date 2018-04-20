package jig.domain.model.declaration.annotation;

import java.util.List;

public interface AnnotationDeclarationRepository {

    List<FieldAnnotationDeclaration> findValidationAnnotation();

    void register(FieldAnnotationDeclaration fieldAnnotationDeclaration);

    void register(MethodAnnotationDeclaration methodAnnotationDeclaration);
}
