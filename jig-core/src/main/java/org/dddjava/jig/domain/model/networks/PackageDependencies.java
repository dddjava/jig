package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.identifier.namespace.PackageDepth;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifiers;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * パッケージの依存関係一覧
 */
public class PackageDependencies {

    PackageIdentifiers packages;
    List<PackageDependency> dependencies;

    public PackageDependencies(List<PackageDependency> dependencies, PackageIdentifiers packages) {
        this.dependencies = dependencies;
        this.packages = packages;
    }

    public List<PackageDependency> list() {
        return dependencies;
    }

    public PackageDependencies applyDepth(PackageDepth packageDepth) {
        if (packageDepth.unlimited()) return this;
        List<PackageDependency> list = this.dependencies.stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .distinct()
                .filter(PackageDependency::notSelfRelation)
                .collect(toList());
        return new PackageDependencies(list, packages.applyDepth(packageDepth));
    }

    public PackageIdentifiers allPackages() {
        return packages;
    }

    public DependencyNumber number() {
        return new DependencyNumber(list().size());
    }
}
