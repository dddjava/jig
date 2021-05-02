package org.dddjava.jig.domain.model.models.domains.validations;

import org.dddjava.jig.domain.model.models.jigobject.member.ValidationAnnotatedMembers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * バリデーション一覧
 */
public class Validations {

    List<Validation> list;

    public Validations(List<Validation> list) {
        this.list = list;
    }

    public static Validations validationAngles(ValidationAnnotatedMembers validationAnnotatedMembers) {
        return new Validations(validationAnnotatedMembers.list().stream()
                .map(Validation::new)
                .collect(Collectors.toList()));
    }

    public List<Validation> list() {
        return list.stream()
                .sorted(Comparator.comparing(validation -> validation.typeIdentifier()))
                .collect(Collectors.toList());
    }
}
