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
        PackageIdentifier fromPackage = packageRelation.from();
        PackageIdentifier toPackage = packageRelation.to();
        if (fromPackage.equals(toPackage)) return false; // 上位との相互依存の場合、containsが一致してしまうので回避
        return (left.contains(fromPackage) && right.contains(toPackage)) ||
                (left.contains(toPackage) && right.contains(fromPackage));
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
