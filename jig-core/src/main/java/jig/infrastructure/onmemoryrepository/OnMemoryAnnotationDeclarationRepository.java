package jig.infrastructure.onmemoryrepository;

import jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class OnMemoryAnnotationDeclarationRepository implements AnnotationDeclarationRepository {

    List<FieldAnnotationDeclaration> fieldAnnotationDeclarations = new ArrayList<>();
    List<MethodAnnotationDeclaration> methodAnnotationDeclarations = new ArrayList<>();

    @Override
    public List<ValidationAnnotationDeclaration> findValidationAnnotation() {
        Stream<ValidationAnnotationDeclaration> fieldStream = fieldAnnotationDeclarations.stream()
                // TODO 正規表現の絞り込みをやめる
                .filter(fieldAnnotationDeclaration -> fieldAnnotationDeclaration.annotationType().fullQualifiedName().matches("(javax.validation|org.hibernate.validator).+"))
                .map(ValidationAnnotationDeclaration::new);
        Stream<ValidationAnnotationDeclaration> methodStream = methodAnnotationDeclarations.stream()
                // TODO 正規表現の絞り込みをやめる
                .filter(fieldAnnotationDeclaration -> fieldAnnotationDeclaration.annotationType().fullQualifiedName().matches("(javax.validation|org.hibernate.validator).+"))
                .map(ValidationAnnotationDeclaration::new);

        return Stream.concat(fieldStream, methodStream)
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
