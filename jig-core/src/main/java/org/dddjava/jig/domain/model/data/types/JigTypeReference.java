package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 型の参照。
 * extendsやimplements、フィールドやメソッドなどで使用される。
 *
 * @param id               型ID
 * @param typeAnnotations  参照時に指定された型アノテーション
 * @param typeArgumentList 参照した型が型パラメタを持つ場合に指定される型引数
 */
public record JigTypeReference(TypeIdentifier id,
                               Collection<JigAnnotationReference> typeAnnotations,
                               List<JigTypeArgument> typeArgumentList) {
    public static JigTypeReference fromId(TypeIdentifier id) {
        return new JigTypeReference(id, List.of(), List.of());
    }

    public static JigTypeReference fromId(String id) {
        return fromId(TypeIdentifier.valueOf(id));
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
                .map(jigTypeParameter -> jigTypeParameter.simpleNameWithGenerics())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    String typeArgumentsFqn() {
        if (typeArgumentList.isEmpty()) return "";
        return typeArgumentList.stream()
                .map(typeArgument -> typeArgument.fqnWithGenerics())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public Stream<TypeIdentifier> allTypeIentifierStream() {
        return Stream.of(
                // Type[] の場合は Type[] と Type の2つにする。これでいいかは疑問はあるが、とりあえず。
                id.isArray() ? Stream.of(id, id.unarray()) : Stream.of(id),
                typeAnnotations.stream().map(jigAnnotationReference -> jigAnnotationReference.id()),
                typeArgumentList.stream().flatMap(jigTypeArgument -> jigTypeArgument.jigTypeReference().allTypeIentifierStream())
        ).flatMap(identity -> identity);
    }

    public JigTypeReference convertArray() {
        return new JigTypeReference(id.convertArray(), typeAnnotations, typeArgumentList);
    }
}