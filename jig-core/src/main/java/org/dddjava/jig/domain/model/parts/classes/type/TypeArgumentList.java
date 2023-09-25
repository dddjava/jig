package org.dddjava.jig.domain.model.parts.classes.type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型引数リスト
 */
public class TypeArgumentList {
    private final List<TypeIdentifier> list;

    public TypeArgumentList(List<TypeIdentifier> list) {
        this.list = list;
    }

    public List<TypeIdentifier> list() {
        return list;
    }

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
