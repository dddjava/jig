package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;

/**
 * パッケージの依存関係
 */
public record PackageRelation(Edge<PackageIdentifier> edge) {

    public static PackageRelation from(PackageIdentifier from, PackageIdentifier to) {
        return new PackageRelation(new Edge<>(from, to));
    }

    public PackageIdentifier from() {
        return edge.from();
    }

    public PackageIdentifier to() {
        return edge.to();
    }

    public PackageRelation applyDepth(PackageDepth packageDepth) {
        return from(from().applyDepth(packageDepth), to().applyDepth(packageDepth));
    }

    public boolean notSelfRelation() {
        return !from().equals(to());
    }
}
