package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;

import java.util.Objects;

/**
 * パッケージの依存関係
 */
public class PackageDependency {
    PackageIdentifier from;
    PackageIdentifier to;

    public PackageDependency(PackageIdentifier from, PackageIdentifier to) {
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
        PackageDependency packageDependency = (PackageDependency) o;
        return Objects.equals(from, packageDependency.from) &&
                Objects.equals(to, packageDependency.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    public PackageDependency applyDepth(PackageDepth packageDepth) {
        return new PackageDependency(from.applyDepth(packageDepth), to.applyDepth(packageDepth));
    }

    public boolean notSelfRelation() {
        return !from.equals(to);
    }
}
