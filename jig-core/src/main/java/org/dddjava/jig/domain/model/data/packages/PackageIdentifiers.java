package org.dddjava.jig.domain.model.data.packages;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * パッケージ識別子一覧
 */
public record PackageIdentifiers(Set<PackageIdentifier> identifiers) {

    public PackageIdentifiers applyDepth(PackageDepth packageDepth) {
        Set<PackageIdentifier> set = identifiers.stream()
                .map(identifier -> identifier.applyDepth(packageDepth))
                .collect(Collectors.toSet());
        return new PackageIdentifiers(set);
    }

    public PackageDepth maxDepth() {
        return identifiers.stream()
                .map(PackageIdentifier::depth)
                .max(Comparator.comparing(PackageDepth::value))
                .orElseGet(() -> new PackageDepth(0));
    }

    public PackageNumber number() {
        return new PackageNumber(identifiers.size());
    }
}
