package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型パラメーター一覧
 */
public class TypeParameters {
    List<TypeParameter> list;

    public TypeParameters(List<TypeParameter> list) {
        this.list = list;
    }

    public List<TypeParameter> list() {
        return list;
    }

    public String asSimpleText() {
        return list.stream()
                .map(e -> e.typeIdentifier.asSimpleText())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public TypeParameter get(int i) {
        return list.get(i);
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
