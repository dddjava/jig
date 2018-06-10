package org.dddjava.jig.domain.model.networks.packages;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * パッケージの依存関係一覧
 */
public class PackageDependencies {

    List<PackageDependency> dependencies;

    public PackageDependencies(List<PackageDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<PackageDependency> list() {
        return dependencies;
    }

    public PackageDependencies applyDepth(PackageDepth packageDepth) {
        List<PackageDependency> list = this.dependencies.stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .distinct()
                .filter(PackageDependency::notSelfRelation)
                .collect(toList());
        return new PackageDependencies(list);
    }

    public DependencyNumber number() {
        return new DependencyNumber(list().size());
    }
}
