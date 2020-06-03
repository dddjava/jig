package org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.packages;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;

/**
 * 相互依存
 */
public class BidirectionalRelation {
    PackageRelation packageRelation;

    public BidirectionalRelation(PackageRelation packageRelation) {
        this.packageRelation = packageRelation;
    }

    public boolean matches(PackageRelation packageRelation) {
        PackageIdentifier left = this.packageRelation.from;
        PackageIdentifier right = this.packageRelation.to;
        return (left.equals(packageRelation.from()) && right.equals(packageRelation.to())) ||
                (left.equals(packageRelation.to()) && right.equals(packageRelation.from()));
    }

    @Override
    public String toString() {
        return packageRelation.from.asText() + " <-> " + packageRelation.to.asText();
    }

    public PackageRelation packageRelation() {
        return packageRelation;
    }
}
