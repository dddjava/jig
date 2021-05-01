package org.dddjava.jig.domain.model.models.domains.validations;

import org.dddjava.jig.domain.model.parts.annotation.ValidationAnnotatedMembers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationAngles {

    List<ValidationAngle> list;

    public ValidationAngles(List<ValidationAngle> list) {
        this.list = list;
    }

    public static ValidationAngles validationAngles(ValidationAnnotatedMembers validationAnnotatedMembers) {
        return new ValidationAngles(validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList()));
    }

    public List<ValidationAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(validationAngle -> validationAngle.typeIdentifier()))
                .collect(Collectors.toList());
    }
}
