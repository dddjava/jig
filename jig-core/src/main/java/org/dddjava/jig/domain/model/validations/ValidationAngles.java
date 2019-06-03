package org.dddjava.jig.domain.model.validations;

import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.annotation.ValidationAnnotatedMembers;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationAngles {

    List<ValidationAngle> list;

    public ValidationAngles(AnalyzedImplementation implementations) {
        ValidationAnnotatedMembers validationAnnotatedMembers = implementations.typeByteCodes().validationAnnotatedMembers();

        list = validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList());
    }

    public List<ValidationAngle> list() {
        return list;
    }
}
