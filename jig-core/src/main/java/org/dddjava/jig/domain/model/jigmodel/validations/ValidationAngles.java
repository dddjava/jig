package org.dddjava.jig.domain.model.jigmodel.validations;

import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMembers;

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
        return list;
    }
}
