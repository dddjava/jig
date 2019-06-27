package org.dddjava.jig.domain.model.declaration.type;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifiers;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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

    public PackageIdentifiers packages() {
        List<TypeIdentifier> typeIdentifiers = list.stream()
                .map(type -> type.identifier())
                .collect(Collectors.toList());
        return new TypeIdentifiers(typeIdentifiers).packageIdentifiers();
    }
}
