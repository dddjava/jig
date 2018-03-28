package jig.domain.model.relation.dependency;

import jig.domain.model.identifier.PackageIdentifiers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PackageDependencies {

    List<PackageDependency> list;
    PackageIdentifiers allPackages;

    public PackageDependencies(List<PackageDependency> list) {
        this.list = list;
        // クラス名昇順、メソッド名昇順
        list.sort(Comparator.<PackageDependency, String>comparing(relation -> relation.from().value())
                .thenComparing(relation -> relation.to().value()));

        allPackages = new PackageIdentifiers(Stream.concat(
                list().stream().map(PackageDependency::from),
                list().stream().map(PackageDependency::to))
                .distinct()
                .collect(toList()));
    }

    public PackageDependencies(List<PackageDependency> list, PackageIdentifiers allPackages) {
        this.list = list;
        this.allPackages = allPackages;
    }

    public List<PackageDependency> list() {
        return list;
    }

    public PackageDependencies applyDepth(Depth depth) {
        if (depth.unlimited()) return this;
        List<PackageDependency> list = this.list.stream()
                .map(relation -> relation.applyDepth(depth))
                .distinct()
                .filter(PackageDependency::notSelfRelation)
                .collect(toList());
        return new PackageDependencies(list);
    }

    public PackageIdentifiers allPackages() {
        return allPackages;
    }
}
