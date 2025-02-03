package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record JigBaseTypeAttributeData(Collection<JigAnnotationData> typeAnnotations,
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
