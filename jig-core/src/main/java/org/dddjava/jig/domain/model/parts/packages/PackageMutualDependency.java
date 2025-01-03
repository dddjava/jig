package org.dddjava.jig.domain.model.parts.packages;

/**
 * パッケージの相互依存
 */
public class PackageMutualDependency {

    private final PackageIdentifier left;
    private final PackageIdentifier right;

    public static PackageMutualDependency from(PackageRelation packageRelation) {
        return new PackageMutualDependency(packageRelation.from, packageRelation.to);
    }
    
    private PackageMutualDependency(PackageIdentifier left, PackageIdentifier right) {
        this.left = left;
        this.right = right;
    }

    public boolean matches(PackageRelation packageRelation) {
        return (left.contains(packageRelation.from()) && right.contains(packageRelation.to())) ||
                (left.contains(packageRelation.to()) && right.contains(packageRelation.from()));
    }

    @Override
    public String toString() {
        return left.asText() + " <-> " + right.asText();
    }

    /**
     * DOT言語での関連出力。
     * 双方向とするため、 edge[dir=both] で囲むこと。
     */
    String dotText() {
        return "\"%s\" -> \"%s\";".formatted(left.asText(), right.asText());
    }
}
