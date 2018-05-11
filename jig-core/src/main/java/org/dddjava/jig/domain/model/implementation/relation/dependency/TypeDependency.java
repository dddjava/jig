package org.dddjava.jig.domain.model.implementation.relation.dependency;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

import java.util.function.Predicate;

public class TypeDependency {

    final TypeIdentifier from;
    final TypeIdentifier to;

    public TypeDependency(TypeIdentifier from, TypeIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean bothMatch(Predicate<TypeIdentifier> predicate) {
        return predicate.test(from) && predicate.test(to);
    }

    public PackageDependency toPackageDependency() {
        return new PackageDependency(from.packageIdentifier(), to.packageIdentifier());
    }
}
