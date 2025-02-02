package org.dddjava.jig.infrastructure.asm.data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record JigBaseTypeAttributeData(Collection<JigAnnotationData> declarationAnnotations,
                                       List<JigTypeParameter> typeParameters) {

    public String typeParametersSimpleName() {
        if (typeParameters.isEmpty()) return "";
        return typeParameters.stream()
                .map(jigTypeParameter -> jigTypeParameter.name())
                .collect(Collectors.joining(", ", "<", ">"));
    }
}
