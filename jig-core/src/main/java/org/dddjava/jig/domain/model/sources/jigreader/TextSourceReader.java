package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;

/**
 * テキストソース読み取り機
 */
public class TextSourceReader {

    JavaTextSourceReader javaTextSourceReader;

    public TextSourceReader(JavaTextSourceReader javaTextSourceReader) {
        this.javaTextSourceReader = javaTextSourceReader;
    }

    public TextSourceModel readTextSource(TextSources textSources) {
        return javaTextSourceReader.textSourceModel(textSources);
    }
}
