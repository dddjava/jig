package org.dddjava.jig.domain.model.networks.type;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.networks.packages.PackageRelation;

/**
 * 型の依存関係
 */
public class TypeRelation {

    final TypeIdentifier from;
    final TypeIdentifier to;

    public TypeRelation(TypeIdentifier from, TypeIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public PackageRelation toPackageDependency() {
        return new PackageRelation(from.packageIdentifier(), to.packageIdentifier());
    }

    public boolean toIs(TypeIdentifier typeIdentifier) {
        return to.equals(typeIdentifier);
    }

    public TypeIdentifier from() {
        return from;
    }

    public boolean notSelfDependency() {
        return !from.normalize().equals(to.normalize());
    }

    public TypeIdentifier to() {
        return to;
    }
}
