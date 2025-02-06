package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * extendsおよびimplements
 */
public record JigBaseTypeData(TypeIdentifier id,
                              Collection<JigAnnotationInstance> typeAnnotations, // typeAnnotationの収集未実装
                              List<JigTypeArgument> typeArgumentList) {
    public static JigBaseTypeData fromId(TypeIdentifier id) {
        return new JigBaseTypeData(id, List.of(), List.of());
    }

    public static JigBaseTypeData fromJvmBinaryName(String jvmBinaryName) {
        return fromId(TypeIdentifier.fromJvmBinaryName(jvmBinaryName));
    }

    public String simpleName() {
        return id.asSimpleText();
    }

    public String simpleNameWithGenerics() {
        return simpleName() + typeArgumentSimpleName();
    }

    public String fqnWithGenerics() {
        return id.value() + typeArgumentsFqn();
    }

    public String fqn() {
        return id.value();
    }

    String typeArgumentSimpleName() {
        if (typeArgumentList.isEmpty()) return "";
        return typeArgumentList.stream()
                .map(jigTypeParameter -> jigTypeParameter.simpleName())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    String typeArgumentsFqn() {
        if (typeArgumentList.isEmpty()) return "";
        return typeArgumentList.stream()
                .map(typeArgument -> typeArgument.value())
                .collect(Collectors.joining(", ", "<", ">"));
    }
}