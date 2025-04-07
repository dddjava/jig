package org.dddjava.jig.domain.model.sources;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * コードのパス
 */
public record SourceBasePath(Collection<Path> paths) {

    public List<Path> pathList() {
        return paths.stream()
                .sorted().distinct()
                .toList();
    }

    public SourceBasePath merge(SourceBasePath other) {
        return new SourceBasePath(Stream.concat(paths.stream(), other.paths.stream()).toList());
    }
}
