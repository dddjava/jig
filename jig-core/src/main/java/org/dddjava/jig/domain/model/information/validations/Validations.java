package org.dddjava.jig.domain.model.information.validations;

import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.information.method.JigMethod;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バリデーション一覧
 */
public class Validations {

    List<Validation> list;

    public Validations(List<Validation> list) {
        this.list = list;
    }

    public static Validations from(JigTypes jigTypes) {
        List<Validation> list = jigTypes.stream()
                .flatMap(Validations::validationAnnotatedMembers)
                .collect(Collectors.toList());
        return new Validations(list);
    }

    static Stream<Validation> validationAnnotatedMembers(JigType jigType) {
        Stream<Validation> methodStream = jigType.instanceJigMethodStream()
                .map(JigMethod::methodAnnotations)
                .map(MethodAnnotations::list)
                .flatMap(List::stream)
                // TODO 正規表現の絞り込みをやめる
                .filter(annotation -> annotation.annotationType().fullQualifiedName().matches("((javax|jakarta).validation|org.hibernate.validator).+"))
                .map(ValidationAnnotatedMember::new)
                .map(Validation::new);
        Stream<Validation> fieldStream = jigType.jigTypeMembers().jigFieldHeaders().stream()
                .flatMap(jigFieldHeader -> jigFieldHeader.jigFieldAttribute().declarationAnnotations().stream()
                        .filter(jigAnnotationReference -> jigAnnotationReference.id().fullQualifiedName().matches("((javax|jakarta).validation|org.hibernate.validator).+"))
                        .map(jigAnnotationReference -> {
                            return new Validation(
                                    jigType.identifier(),
                                    jigFieldHeader.name(),
                                    jigFieldHeader.jigTypeReference().id(),
                                    jigAnnotationReference.id(),
                                    jigAnnotationReference.asText()
                            );
                        }));
        return Stream.concat(fieldStream, methodStream);
    }

    public List<Validation> list() {
        return list.stream()
                .sorted(Comparator.comparing(validation -> validation.typeIdentifier()))
                .collect(Collectors.toList());
    }
}
