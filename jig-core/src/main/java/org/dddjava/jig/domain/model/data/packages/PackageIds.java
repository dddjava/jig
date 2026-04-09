package org.dddjava.jig.domain.model.data.packages;

import java.util.Comparator;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * パッケージ識別子一覧
 */
public record PackageIds(Set<PackageId> values) {

    public PackageIds applyDepth(PackageDepth packageDepth) {
        Set<PackageId> set = values.stream()
                .map(packageId -> packageId.applyDepth(packageDepth))
                .collect(toSet());
        return new PackageIds(set);
    }

    public PackageDepth maxDepth() {
        return values.stream()
                .map(PackageId::depth)
                .max(Comparator.comparing(PackageDepth::value))
                .orElseGet(() -> new PackageDepth(0));
    }

    public int size() {
        return values.size();
    }
}
