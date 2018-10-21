package org.dddjava.jig.domain.model.networks.packages;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;

/**
 * 相互依存
 */
public class BidirectionalRelation {
    PackageIdentifier left;
    PackageIdentifier right;

    public BidirectionalRelation(PackageIdentifier left, PackageIdentifier right) {
        this.left = left;
        this.right = right;
    }

    public boolean matches(PackageRelation packageRelation) {
        return (left.equals(packageRelation.from()) && right.equals(packageRelation.to())) ||
                (left.equals(packageRelation.to()) && right.equals(packageRelation.from()));
    }

    public PackageIdentifier left() {
        return left;
    }

    public PackageIdentifier right() {
        return right;
    }
}
