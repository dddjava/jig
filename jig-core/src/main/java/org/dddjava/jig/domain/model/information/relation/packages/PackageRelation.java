package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;

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
}
