package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;

/**
 * パッケージの相互依存
 */
public record PackageMutualDependency(PackageIdentifier left, PackageIdentifier right) {

    public static PackageMutualDependency from(PackageRelation packageRelation) {
        return new PackageMutualDependency(packageRelation.from(), packageRelation.to());
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
