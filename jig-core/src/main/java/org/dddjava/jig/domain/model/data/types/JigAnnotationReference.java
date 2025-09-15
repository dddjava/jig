package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * アノテーション参照
 * 宣言アノテーションや型アノテーションとして記述されたもの
 * 「参照」と言う名前をとっているが、アノテーションの要素値を持つため実体という方が正しい。
 * JigTypeReferenceと位置付けが近いから寄せている。
 *
 * @param id アノテーションの型を示すID
 * @param elements 要素のコレクション
 * @see JigTypeReference
 */
public record JigAnnotationReference(TypeId id,
                                     Collection<JigAnnotationElementValuePair> elements) {

    public String simpleTypeName() {
        return id.asSimpleText();
    }

    public Stream<TypeId> allTypeIdStream() {
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
