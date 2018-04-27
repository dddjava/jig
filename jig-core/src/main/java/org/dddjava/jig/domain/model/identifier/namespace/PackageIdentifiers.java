package org.dddjava.jig.domain.model.identifier.namespace;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PackageIdentifiers {

    List<PackageIdentifier> list;

    public PackageIdentifiers(List<PackageIdentifier> list) {
        this.list = list;
    }

    public Stream<PackageIdentifier> stream() {
        return list.stream();
    }

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
}
