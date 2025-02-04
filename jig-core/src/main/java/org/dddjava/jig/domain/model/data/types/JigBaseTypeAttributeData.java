package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record JigBaseTypeAttributeData(Collection<JigAnnotationInstance> typeAnnotations, // typeAnnotationの収集未実装
                                       List<JigTypeArgument> typeArgumentList) {

    public static JigBaseTypeAttributeData empty() {
        return new JigBaseTypeAttributeData(List.of(), List.of());
    }

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
