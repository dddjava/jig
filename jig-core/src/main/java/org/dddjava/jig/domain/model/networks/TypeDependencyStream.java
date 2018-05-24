package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.stream.Stream;

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
