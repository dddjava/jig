package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageId;

/**
 * パッケージの依存関係
 */
public record PackageRelation(PackageId from, PackageId to) {

    public static PackageRelation from(PackageId from, PackageId to) {
        return new PackageRelation(from, to);
    }
}
