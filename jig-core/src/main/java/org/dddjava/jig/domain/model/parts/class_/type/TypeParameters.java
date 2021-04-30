package org.dddjava.jig.domain.model.parts.class_.type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型パラメーター一覧
 */
public class TypeParameters {
    List<TypeIdentifier> list;

    public TypeParameters(List<TypeIdentifier> list) {
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
