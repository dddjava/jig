package org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class PackageTree {

    private List<PackageIdentifier> values;

    public PackageTree(List<PackageIdentifier> values) {
        this.values = values;
    }

    public static PackageTree of(List<PackageIdentifier> list) {
        return new PackageTree(list);
    }

    public Map<PackageIdentifier, List<PackageIdentifier>> map() {
        return values.stream()
                .collect(groupingBy(PackageIdentifier::parent));
    }
}
