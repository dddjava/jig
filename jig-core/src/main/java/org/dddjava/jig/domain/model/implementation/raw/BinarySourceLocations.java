package org.dddjava.jig.domain.model.implementation.raw;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バイナリソースの場所
 */
public class BinarySourceLocations {

    List<Path> list;

    public BinarySourceLocations(Collection<Path> collection) {
        this.list = new ArrayList<>(collection);
    }

    public List<Path> paths() {
        return list;
    }

    public BinarySourceLocations merge(BinarySourceLocations other) {
        return new BinarySourceLocations(Stream.concat(list.stream(), other.list.stream()).collect(Collectors.toList()));
    }
}
