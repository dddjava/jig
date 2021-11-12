package org.dddjava.jig.domain.model.parts.packages;

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
        return (left.contains(packageRelation.from()) && right.contains(packageRelation.to())) ||
                (left.contains(packageRelation.to()) && right.contains(packageRelation.from()));
    }

    @Override
    public String toString() {
        return packageRelation.from.asText() + " <-> " + packageRelation.to.asText();
    }
}
