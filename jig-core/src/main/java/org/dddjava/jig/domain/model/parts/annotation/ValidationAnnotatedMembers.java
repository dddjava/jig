package org.dddjava.jig.domain.model.parts.annotation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バリデーションアノテーションのついたメンバ一覧
 */
public class ValidationAnnotatedMembers {

    FieldAnnotations fieldAnnotations;
    MethodAnnotations methodAnnotations;

    public ValidationAnnotatedMembers(FieldAnnotations fieldAnnotations, MethodAnnotations methodAnnotations) {
        this.fieldAnnotations = fieldAnnotations;
        this.methodAnnotations = methodAnnotations;
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
