package org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_;

import java.util.Comparator;
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

    public PackageIdentifier rootPackage() {
        //TODO: このとり方で良い？複数あるときはやっちゃだめじゃない？
        return map().keySet().stream()
                .min(Comparator.comparingInt(o -> o.depth().value))
                .orElseGet(PackageIdentifier::defaultPackage);
    }
}
