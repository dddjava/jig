package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.jigfactory.JavaSourceModel;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaTextSourceReader {

    JavaSourceModel textSourceModel(TextSources textSources);
}
