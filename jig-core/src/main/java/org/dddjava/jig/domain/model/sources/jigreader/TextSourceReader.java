package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;

/**
 * テキストソース読み取り機
 */
public class TextSourceReader {

    JavaTextSourceReader javaTextSourceReader;
    KotlinTextSourceReader kotlinTextSourceReader;
    ScalaSourceAliasReader scalaSourceAliasReader;

    public TextSourceReader(JavaTextSourceReader javaTextSourceReader, AdditionalTextSourceReader additionalTextSourceReader) {
        this.javaTextSourceReader = javaTextSourceReader;
        this.kotlinTextSourceReader = additionalTextSourceReader.kotlinTextSourceReader();
        this.scalaSourceAliasReader = additionalTextSourceReader.scalaSourceAliasReader();
    }

    public TextSourceModel readTextSource(TextSources textSources) {
        var javaTextSourceModel = javaTextSourceReader.textSourceModel(textSources);
        return javaTextSourceModel.addClassAndMethodComments(
                kotlinTextSourceReader.readClasses(textSources.kotlinSources()),
                scalaSourceAliasReader.readAlias(textSources.scalaSources())
        );
    }
}
