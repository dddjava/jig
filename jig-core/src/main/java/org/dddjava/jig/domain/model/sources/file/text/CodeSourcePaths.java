package org.dddjava.jig.domain.model.sources.file.text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * コードのパス
 */
public class CodeSourcePaths {

    List<Path> list;

    public CodeSourcePaths(Collection<Path> collection) {
        this.list = new ArrayList<>(collection);
    }

    public List<Path> paths() {
        return list.stream()
                .sorted().distinct()
                .collect(Collectors.toList());
    }

    public CodeSourcePaths merge(CodeSourcePaths other) {
        return new CodeSourcePaths(Stream.concat(list.stream(), other.list.stream()).collect(Collectors.toList()));
    }
}
