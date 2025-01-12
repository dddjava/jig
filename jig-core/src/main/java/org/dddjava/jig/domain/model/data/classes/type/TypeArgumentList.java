package org.dddjava.jig.domain.model.data.classes.type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型引数リスト
 *
 * TODO この形だと {@code A<B<C>>} などのネストに対応できない
 */
public record TypeArgumentList(List<TypeIdentifier> list) {

    public String asSimpleText() {
        // <Hoge, Fuga> の形
        return list.stream()
                .map(TypeIdentifier::asSimpleText)
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
