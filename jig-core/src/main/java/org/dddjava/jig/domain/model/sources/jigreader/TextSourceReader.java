package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.packages.PackageComments;
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

    public PackageComments readPackageComments(TextSources textSources) {
        return javaTextSourceReader.readPackages(textSources.packageInfoSources());
    }

    public TextSourceModel readTextSource(TextSources textSources) {
        TextSourceModel javaTextSourceModel = javaTextSourceReader.readClasses(textSources.javaSources());
        return javaTextSourceModel.addClassAndMethodComments(
                kotlinTextSourceReader.readClasses(textSources.kotlinSources()),
                scalaSourceAliasReader.readAlias(textSources.scalaSources())
        );
    }
}
