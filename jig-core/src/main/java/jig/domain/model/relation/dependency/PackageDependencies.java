package jig.domain.model.relation.dependency;

import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.identifier.namespace.PackageIdentifiers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PackageDependencies {

    PackageIdentifiers packages;
    List<PackageDependency> dependencies;

    // TODO jdepsがいらなくなったら不要になる
    public PackageDependencies(List<PackageDependency> dependencies) {
        this.dependencies = dependencies;
        // クラス名昇順、メソッド名昇順
        dependencies.sort(Comparator.<PackageDependency, String>comparing(relation -> relation.from().value())
                .thenComparing(relation -> relation.to().value()));

        packages = new PackageIdentifiers(Stream.concat(
                list().stream().map(PackageDependency::from),
                list().stream().map(PackageDependency::to))
                .distinct()
                .collect(toList()));
    }

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

    // TODO jdepsがいらなくなったら不要になる
    public PackageDependencies withAllPackage(PackageIdentifiers allPackages) {
        return new PackageDependencies(this.dependencies, allPackages);
    }

    public int size() {
        return list().size();
    }
}
