package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * 宣言アノテーションや型アノテーションとして記述されたアノテーション
 *
 * @param id アノテーションの型を示すID
 * @param elements 要素のコレクション
 */
public record JigAnnotationReference(TypeId id,
                                     Collection<JigAnnotationElementValuePair> elements) {

    public static JigAnnotationReference from(TypeId typeId) {
        return new JigAnnotationReference(typeId, List.of());
    }

    public String simpleTypeName() {
        return id.asSimpleText();
    }

    public Stream<TypeId> allTypeIentifierStream() {
        // TODO elementがclassやannotationの場合に追加する
        return Stream.of(id);
    }

    public Optional<String> elementTextOf(String name) {
        return elements.stream()
                .filter(element -> element.name().equals(name))
                .map(element -> element.valueAsString())
                .findAny();
    }

    public String asText() {
        return elements.stream()
                .map(element -> "%s=%s".formatted(element.name(), element.valueAsString()))
                .collect(joining(", ", "[", "]"));
    }
}
