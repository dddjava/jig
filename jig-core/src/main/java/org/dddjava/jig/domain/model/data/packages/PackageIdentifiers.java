package org.dddjava.jig.domain.model.data.packages;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * パッケージ識別子一覧
 */
public record PackageIdentifiers(List<PackageIdentifier> list) {

    public PackageIdentifiers applyDepth(PackageDepth packageDepth) {
        List<PackageIdentifier> list = this.list.stream()
                .map(identifier -> identifier.applyDepth(packageDepth))
                .distinct()
                .collect(toList());
        return new PackageIdentifiers(list);
    }

    public PackageDepth maxDepth() {
        return list.stream()
                .map(PackageIdentifier::depth)
                .max(Comparator.comparing(PackageDepth::value))
                .orElseGet(() -> new PackageDepth(0));
    }

    public PackageNumber number() {
        return new PackageNumber(list.size());
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public PackageIdentifiers parent() {
        return applyDepth(new PackageDepth(maxDepth().value() - 1));
    }
}
