package org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.packages;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;

import java.util.Objects;

/**
 * パッケージの依存関係
 */
public class PackageRelation {
    PackageIdentifier from;
    PackageIdentifier to;

    public PackageRelation(PackageIdentifier from, PackageIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public static PackageRelation fromClassRelation(ClassRelation classRelation) {
        return new PackageRelation(classRelation.from().packageIdentifier(), classRelation.to().packageIdentifier());
    }

    public PackageIdentifier from() {
        return from;
    }

    public PackageIdentifier to() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageRelation packageRelation = (PackageRelation) o;
        return Objects.equals(from, packageRelation.from) &&
                Objects.equals(to, packageRelation.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    public PackageRelation applyDepth(PackageDepth packageDepth) {
        return new PackageRelation(from.applyDepth(packageDepth), to.applyDepth(packageDepth));
    }

    public boolean notSelfRelation() {
        return !from.equals(to);
    }

    public boolean bothMatch(PackageIdentifiers packageIdentifiers) {
        return packageIdentifiers.contains(from) && packageIdentifiers.contains(to);
    }

    boolean matches(TypeIdentifier fromTypeIdentifier, TypeIdentifier toTypeIdentifier) {
        return fromTypeIdentifier.belongs(from) && toTypeIdentifier.belongs(to);
    }
}
