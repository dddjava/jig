package org.dddjava.jig.domain.model.implementation.raw;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextSourceLocations {

    List<Path> list;

    public TextSourceLocations(Collection<Path> collection) {
        this.list = new ArrayList<>(collection);
    }

    public List<Path> paths() {
        return list;
    }

    public TextSourceLocations merge(TextSourceLocations other) {
        return new TextSourceLocations(Stream.concat(list.stream(), other.list.stream()).collect(Collectors.toList()));
    }
}
