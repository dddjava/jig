package org.dddjava.jig.domain.model.implementation.networks.packages;

import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageIdentifiers;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * パッケージの依存関係一覧
 */
public class PackageRelations {

    List<PackageRelation> dependencies;

    public PackageRelations(List<PackageRelation> dependencies) {
        this.dependencies = dependencies;
    }

    public List<PackageRelation> list() {
        return dependencies;
    }

    public PackageRelations applyDepth(PackageDepth packageDepth) {
        List<PackageRelation> list = this.dependencies.stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .distinct()
                .filter(PackageRelation::notSelfRelation)
                .collect(toList());
        return new PackageRelations(list);
    }

    public RelationNumber number() {
        return new RelationNumber(list().size());
    }

    public PackageRelations filterBothMatch(PackageIdentifiers packageIdentifiers) {
        List<PackageRelation> list = dependencies.stream()
                .filter(packageDependency -> packageDependency.bothMatch(packageIdentifiers))
                .collect(Collectors.toList());
        return new PackageRelations(list);
    }

    public boolean available() {
        return !dependencies.isEmpty();
    }
}
