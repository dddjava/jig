package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 型の一覧
 */
public class Types {
    List<Type> list;

    public Types(List<Type> list) {
        this.list = list;
    }

    public Type get(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(e -> e.identifier().equals(typeIdentifier))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(typeIdentifier.toString()));
    }

    public List<Type> list() {
        return list;
    }
}
