package org.dddjava.jig.domain.model.sources;

/**
 * ソース読み取り機
 */
public interface SourceReader {

    Sources readSources(SourceBasePaths sourceBasePaths);
}
