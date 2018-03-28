package jig.domain.model.identifier;

import java.util.List;
import java.util.stream.Stream;

public class PackageIdentifiers {

    List<PackageIdentifier> list;

    public PackageIdentifiers(List<PackageIdentifier> list) {
        this.list = list;
    }

    public Stream<PackageIdentifier> stream() {
        return list.stream();
    }
}
