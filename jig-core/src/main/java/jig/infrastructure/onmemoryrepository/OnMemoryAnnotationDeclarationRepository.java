package jig.infrastructure.onmemoryrepository;

import jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OnMemoryAnnotationDeclarationRepository implements AnnotationDeclarationRepository {

    List<FieldAnnotationDeclaration> fieldAnnotationDeclarations = new ArrayList<>();
    List<MethodAnnotationDeclaration> methodAnnotationDeclarations = new ArrayList<>();

    @Override
    public List<FieldAnnotationDeclaration> findValidationAnnotation() {
        return fieldAnnotationDeclarations.stream()
                // TODO 正規表現の絞り込みをやめる
                .filter(fieldAnnotationDeclaration -> fieldAnnotationDeclaration.annotationType().fullQualifiedName().matches("(javax.validation|org.hibernate.validator).+"))
                .collect(Collectors.toList());
    }

    @Override
    public void register(FieldAnnotationDeclaration fieldAnnotationDeclaration) {
        fieldAnnotationDeclarations.add(fieldAnnotationDeclaration);
    }

    @Override
    public void register(MethodAnnotationDeclaration methodAnnotationDeclaration) {
        methodAnnotationDeclarations.add(methodAnnotationDeclaration);
    }
}
