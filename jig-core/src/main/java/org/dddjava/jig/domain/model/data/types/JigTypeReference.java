package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * 型の参照。
 * extendsやimplements、フィールドやメソッドなどで使用される。
 * 「参照」と言う名前であり型そのものではないが、型パラメタを持つ実体でもある。
 *
 * @param id               型ID
 * @param typeAnnotations  参照時に指定された型アノテーション
 * @param typeArgumentList 参照した型が型パラメタを持つ場合に指定される型引数
 */
public record JigTypeReference(TypeId id,
                               Collection<JigAnnotationReference> typeAnnotations,
                               List<JigTypeArgument> typeArgumentList) {
    public static JigTypeReference fromId(TypeId id) {
        return new JigTypeReference(id, List.of(), List.of());
    }

    public static JigTypeReference fromJvmBinaryName(String jvmBinaryName) {
        return fromId(TypeId.fromJvmBinaryName(jvmBinaryName));
    }

    public String simpleName() {
        return id.asSimpleText();
    }

    public String simpleNameWithGenerics() {
        return simpleName() + formatTypeArguments(JigTypeArgument::simpleNameWithGenerics);
    }

    public String fqnWithGenerics() {
        return id.value() + formatTypeArguments(JigTypeArgument::fqnWithGenerics);
    }

    public String fqn() {
        return id.value();
    }

    private String formatTypeArguments(Function<JigTypeArgument, String> jigTypeArgumentFormatter) {
        if (typeArgumentList.isEmpty()) return "";
        return typeArgumentList.stream()
                .map(jigTypeArgumentFormatter)
                .collect(joining(", ", "<", ">"));
    }

    public Stream<TypeId> toTypeIdStream() {
        if (id.isVoid()) return Stream.empty();

        return Stream.of(
                        // Type[] の場合は Type[] と Type の2つにする。これでいいかは疑問はあるが、とりあえず。
                        id.isArray() ? Stream.of(id, id.unarray()) : Stream.of(id),
                        typeAnnotations.stream().map(JigAnnotationReference::id),
                        typeArgumentList.stream().flatMap(jigTypeArgument -> jigTypeArgument.jigTypeReference().toTypeIdStream())
                )
                .flatMap(identity -> identity);
    }

    public JigTypeReference convertArray() {
        return new JigTypeReference(id.convertArray(), typeAnnotations, typeArgumentList);
    }
}