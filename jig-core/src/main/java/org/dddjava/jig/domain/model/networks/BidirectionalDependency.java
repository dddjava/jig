package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;

/**
 * 相互依存
 */
public class BidirectionalDependency {
    PackageIdentifier left;
    PackageIdentifier right;

    public BidirectionalDependency(PackageIdentifier left, PackageIdentifier right) {
        this.left = left;
        this.right = right;
    }

    public boolean matches(PackageDependency packageDependency) {
        return (left.equals(packageDependency.from()) && right.equals(packageDependency.to())) ||
                (left.equals(packageDependency.to()) && right.equals(packageDependency.from()));
    }

    public PackageIdentifier left() {
        return left;
    }

    public PackageIdentifier right() {
        return right;
    }
}
