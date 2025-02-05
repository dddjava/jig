package org.dddjava.jig.domain.model.sources.javasources;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * テキストソース一覧
 */
public record JavaSources(List<Path> paths) {

    public boolean nothing() {
        return paths.isEmpty();
    }

    public Collection<Path> packageInfoPaths() {
        return paths.stream()
                .filter(path -> path.endsWith("package-info.java"))
                .toList();
    }
}
