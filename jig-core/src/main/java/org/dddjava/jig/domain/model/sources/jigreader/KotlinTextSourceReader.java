package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSources;
import org.dddjava.jig.domain.model.sources.jigfactory.ClassAndMethodComments;

/**
 * Kotlinのテキストソースを読み取る
 */
public interface KotlinTextSourceReader {

    ClassAndMethodComments readClasses(ReadableTextSources readableTextSources);
}
