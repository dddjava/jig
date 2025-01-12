package org.dddjava.jig.domain.model.data.classes.type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型引数リスト
 */
public record TypeArgumentList(List<TypeIdentifier> list) {

    public String asSimpleText() {
        // <Hoge, Fuga> の形
        return list.stream()
                .map(e -> e.asSimpleText())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
