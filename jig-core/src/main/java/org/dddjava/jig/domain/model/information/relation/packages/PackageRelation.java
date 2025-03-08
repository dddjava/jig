package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;

/**
 * パッケージの依存関係
 */
public record PackageRelation(PackageIdentifier from, PackageIdentifier to) {

    public PackageRelation applyDepth(PackageDepth packageDepth) {
        return new PackageRelation(from().applyDepth(packageDepth), to().applyDepth(packageDepth));
    }

    public boolean notSelfRelation() {
        return !from().equals(to());
    }
}
