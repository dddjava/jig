package org.dddjava.jig.domain.model.implementation.analyzed.declaration.annotation;

import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バリデーションアノテーションのついたメンバ一覧
 */
public class ValidationAnnotatedMembers {

    private final FieldAnnotations fieldAnnotations;
    private final MethodAnnotations methodAnnotations;

    public ValidationAnnotatedMembers(AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes= analyzedImplementation.typeByteCodes();
        this.fieldAnnotations = typeByteCodes.annotatedFields();
        this.methodAnnotations = typeByteCodes.annotatedMethods();
    }

    public List<ValidationAnnotatedMember> list() {
        Stream<ValidationAnnotatedMember> fieldStream = fieldAnnotations.list().stream()
                // TODO 正規表現の絞り込みをやめる
                .filter(fieldAnnotationDeclaration -> fieldAnnotationDeclaration.annotationType().fullQualifiedName().matches("(javax.validation|org.hibernate.validator).+"))
                .map(ValidationAnnotatedMember::new);
        Stream<ValidationAnnotatedMember> methodStream = methodAnnotations.list().stream()
                // TODO 正規表現の絞り込みをやめる
                .filter(methodAnnotationDeclaration -> methodAnnotationDeclaration.annotationType().fullQualifiedName().matches("(javax.validation|org.hibernate.validator).+"))
                .map(ValidationAnnotatedMember::new);

        return Stream.concat(fieldStream, methodStream)
                .collect(Collectors.toList());
    }
}
