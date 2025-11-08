package org.dddjava.jig.domain.model.sources.filesystem;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * テキストソース一覧
 */
public record JavaFilePaths(List<Path> packageInfos, List<Path> javaFiles) {

    public boolean nothing() {
        return packageInfos.isEmpty() && javaFiles.isEmpty();
    }

    public Collection<Path> packageInfoPaths() {
        return packageInfos;
    }

    public Collection<Path> javaPaths() {
        return javaFiles;
    }

    public int size() {
        return packageInfos.size() + javaFiles.size();
    }
}
