package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;

/**
 * パッケージの依存関係
 */
public record PackageRelation(Edge<PackageId> edge) {

    public static PackageRelation from(PackageId from, PackageId to) {
        return new PackageRelation(Edge.of(from, to));
    }

    public PackageId from() {
        return edge.from();
    }

    public PackageId to() {
        return edge.to();
    }
}
