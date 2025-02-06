package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 型の参照。
 * extendsやimplements、フィールドやメソッドなどで使用される。
 *
 * @param id 型ID
 * @param typeAnnotations 参照時に指定された型アノテーション
 * @param typeArgumentList 参照した型が型パラメタを持つ場合に指定される型引数
 */
public record JigTypeReference(TypeIdentifier id,
                               Collection<JigAnnotationInstance> typeAnnotations,
                               List<JigTypeArgument> typeArgumentList) {
    public static JigTypeReference fromId(TypeIdentifier id) {
        return new JigTypeReference(id, List.of(), List.of());
    }

    public static JigTypeReference fromJvmBinaryName(String jvmBinaryName) {
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