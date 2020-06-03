package org.dddjava.jig.domain.model.jigsource.file;

/**
 * ソース読み取り機
 */
public interface SourceReader {

    Sources readSources(SourcePaths sourcePaths);
}
