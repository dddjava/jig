package org.dddjava.jig.domain.model.sources.javasources;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaSourceReader {

    JavaSourceModel textSourceModel(TextSources textSources);
}
