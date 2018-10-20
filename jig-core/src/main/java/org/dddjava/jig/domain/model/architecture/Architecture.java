package org.dddjava.jig.domain.model.architecture;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.List;

public interface Architecture {

    default boolean isService(List<TypeAnnotation> typeAnnotations) {
        TypeIdentifier serviceAnnotation = new TypeIdentifier("org.springframework.stereotype.Service");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(serviceAnnotation));
    }
}
