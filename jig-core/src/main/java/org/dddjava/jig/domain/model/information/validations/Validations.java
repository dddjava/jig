package org.dddjava.jig.domain.model.information.validations;

import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotations;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigInstanceMember;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.member.JigField;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;

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
        List<Validation> list = jigTypes.list().stream()
                .flatMap(Validations::validationAnnotatedMembers)
                .map(Validation::new)
                .collect(Collectors.toList());
        return new Validations(list);
    }

    static Stream<ValidationAnnotatedMember> validationAnnotatedMembers(JigType jigType) {
        JigInstanceMember instanceMember = jigType.instanceMember();
        Stream<ValidationAnnotatedMember> methodStream = instanceMember.instanceMethods().stream()
                .map(JigMethod::methodAnnotations)
                .map(MethodAnnotations::list)
                .flatMap(List::stream)
                // TODO 正規表現の絞り込みをやめる
                .filter(annotation -> annotation.annotationType().fullQualifiedName().matches("((javax|jakarta).validation|org.hibernate.validator).+"))
                .map(ValidationAnnotatedMember::new);
        Stream<ValidationAnnotatedMember> fieldStream = instanceMember.instanceFields().list().stream()
                .map(JigField::fieldAnnotations)
                .map(FieldAnnotations::list)
                .flatMap(List::stream)
                // TODO 正規表現の絞り込みをやめる
                .filter(annotation -> annotation.annotationType().fullQualifiedName().matches("((javax|jakarta).validation|org.hibernate.validator).+"))
                .map(ValidationAnnotatedMember::new);

        return Stream.concat(fieldStream, methodStream);
    }

    public List<Validation> list() {
        return list.stream()
                .sorted(Comparator.comparing(validation -> validation.typeIdentifier()))
                .collect(Collectors.toList());
    }
}
