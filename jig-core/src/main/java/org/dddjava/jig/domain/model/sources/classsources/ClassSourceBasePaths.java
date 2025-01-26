package org.dddjava.jig.domain.model.sources.classsources;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バイナリソースの場所
 */
public class ClassSourceBasePaths {

    List<Path> list;

    public ClassSourceBasePaths(Collection<Path> collection) {
        this.list = new ArrayList<>(collection);
    }

    public List<Path> paths() {
        return list.stream()
                .sorted().distinct()
                .collect(Collectors.toList());
    }

    public ClassSourceBasePaths merge(ClassSourceBasePaths other) {
        return new ClassSourceBasePaths(Stream.concat(list.stream(), other.list.stream()).collect(Collectors.toList()));
    }
}
