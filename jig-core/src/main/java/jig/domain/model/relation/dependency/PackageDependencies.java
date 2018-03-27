package jig.domain.model.relation.dependency;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PackageDependencies {

    List<PackageDependency> list;

    public PackageDependencies(List<PackageDependency> list) {
        this.list = list;
        // クラス名昇順、メソッド名昇順
        list.sort(Comparator.<PackageDependency, String>comparing(relation -> relation.from().value())
                .thenComparing(relation -> relation.to().value()));
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
                .collect(Collectors.toList());
        return new PackageDependencies(list);
    }
}
