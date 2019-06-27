package org.dddjava.jig.domain.model.implementation.analyzed.networks.class_;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.PackageRelation;

/**
 * 型の依存関係
 */
public class ClassRelation {

    final TypeIdentifier from;
    final TypeIdentifier to;

    public ClassRelation(TypeIdentifier from, TypeIdentifier to) {
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

    public boolean selfRelation() {
        return from.normalize().equals(to.normalize());
    }

    public TypeIdentifier to() {
        return to;
    }
}
