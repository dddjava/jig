package org.dddjava.jig.infrastructure.asm.data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record JigBaseTypeAttributeData(Collection<JigAnnotationData> declarationAnnotations,
                                       List<JigTypeArgument> typeArgumentList) {

    public String typeArgumentSimpleName() {
        if (typeArgumentList.isEmpty()) return "";
        return typeArgumentList.stream()
                .map(jigTypeParameter -> jigTypeParameter.simpleName())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public String typeArgumentsFqn() {
        if (typeArgumentList.isEmpty()) return "";
        return typeArgumentList.stream()
                .map(typeArgument -> typeArgument.value())
                .collect(Collectors.joining(", ", "<", ">"));
    }
}
