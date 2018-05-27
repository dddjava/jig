package org.dddjava.jig.domain.model.declaration.annotation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バリデーションアノテーションのついたメンバ一覧
 */
public class ValidationAnnotatedMembers {

    private final AnnotatedFields annotatedFields;
    private final AnnotatedMethods annotatedMethods;

    public ValidationAnnotatedMembers(AnnotatedFields annotatedFields, AnnotatedMethods annotatedMethods) {
        this.annotatedFields = annotatedFields;
        this.annotatedMethods = annotatedMethods;
    }

    public List<ValidationAnnotatedMember> list() {
        Stream<ValidationAnnotatedMember> fieldStream = annotatedFields.list().stream()
                // TODO 正規表現の絞り込みをやめる
                .filter(fieldAnnotationDeclaration -> fieldAnnotationDeclaration.annotationType().fullQualifiedName().matches("(javax.validation|org.hibernate.validator).+"))
                .map(ValidationAnnotatedMember::new);
        Stream<ValidationAnnotatedMember> methodStream = annotatedMethods.list().stream()
                // TODO 正規表現の絞り込みをやめる
                .filter(fieldAnnotationDeclaration -> fieldAnnotationDeclaration.annotationType().fullQualifiedName().matches("(javax.validation|org.hibernate.validator).+"))
                .map(ValidationAnnotatedMember::new);

        return Stream.concat(fieldStream, methodStream)
                .collect(Collectors.toList());
    }
}
