package org.dddjava.jig.domain.model.sources.file;

/**
 * ソース読み取り機
 */
public interface SourceReader {

    Sources readSources(SourcePaths sourcePaths);
}
