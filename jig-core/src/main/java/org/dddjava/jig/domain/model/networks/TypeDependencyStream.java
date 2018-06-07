package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.stream.Stream;

/**
 * 型の依存関係一覧ストリーム
 */
public class TypeDependencyStream {

    Stream<TypeDependency> stream;

    public TypeDependencyStream(Stream<TypeDependency> stream) {
        this.stream = stream;
    }

    public TypeDependencyStream filterTo(TypeIdentifier typeIdentifier) {
        return new TypeDependencyStream(stream.filter(typeDependency -> typeDependency.toIs(typeIdentifier)));
    }

    public TypeIdentifiers fromTypeIdentifiers() {
        return stream.map(TypeDependency::from).collect(TypeIdentifiers.collector());
    }

    public TypeDependencyStream removeSelf() {
        return new TypeDependencyStream(stream.filter(TypeDependency::notSelfDependency));
    }
}
