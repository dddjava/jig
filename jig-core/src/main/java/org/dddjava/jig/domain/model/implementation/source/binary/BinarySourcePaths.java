package org.dddjava.jig.domain.model.implementation.source.binary;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バイナリソースの場所
 */
public class BinarySourcePaths {

    List<Path> list;

    public BinarySourcePaths(Collection<Path> collection) {
        this.list = new ArrayList<>(collection);
    }

    public List<Path> paths() {
        return list;
    }

    public BinarySourcePaths merge(BinarySourcePaths other) {
        return new BinarySourcePaths(Stream.concat(list.stream(), other.list.stream()).collect(Collectors.toList()));
    }
}
