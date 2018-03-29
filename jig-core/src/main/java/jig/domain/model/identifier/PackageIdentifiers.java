package jig.domain.model.identifier;

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
}
