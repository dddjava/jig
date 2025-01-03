package org.dddjava.jig.domain.model.parts.packages;

/**
 * パッケージの相互依存
 */
public class PackageMutualDependency {

    private final PackageIdentifier left;
    private final PackageIdentifier right;

    public PackageMutualDependency(PackageRelation packageRelation) {
        left = packageRelation.from;
        right = packageRelation.to;
    }

    public boolean matches(PackageRelation packageRelation) {
        return (left.contains(packageRelation.from()) && right.contains(packageRelation.to())) ||
                (left.contains(packageRelation.to()) && right.contains(packageRelation.from()));
    }

    @Override
    public String toString() {
        return left.asText() + " <-> " + right.asText();
    }
}
