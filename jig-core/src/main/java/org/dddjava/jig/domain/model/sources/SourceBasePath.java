package org.dddjava.jig.domain.model.sources;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * コードのパス
 */
public class SourceBasePath {

    List<Path> list;

    public SourceBasePath(Collection<Path> collection) {
        this.list = new ArrayList<>(collection);
    }

    public List<Path> paths() {
        return list.stream()
                .sorted().distinct()
                .collect(Collectors.toList());
    }

    public SourceBasePath merge(SourceBasePath other) {
        return new SourceBasePath(Stream.concat(list.stream(), other.list.stream()).collect(Collectors.toList()));
    }
}
