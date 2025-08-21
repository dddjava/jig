package org.dddjava.jig.domain.model.sources.filesystem;

/**
 * ファイルシステムにあるソース群
 *
 * `java.nio.file.Path` で扱うものたち。
 */
public record FilesystemSources(SourceBasePaths sourceBasePaths,
                                JavaFilePaths javaFilePaths,
                                ClassFilePaths classFilePaths) {

    public boolean emptyClassSources() {
        return classFilePaths.nothing();
    }

    public boolean emptyJavaSources() {
        return javaFilePaths.nothing();
    }
}
