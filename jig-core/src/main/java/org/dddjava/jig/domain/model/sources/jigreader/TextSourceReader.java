package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.CodeSources;

import java.util.stream.Stream;

/**
 * コードを使用する別名別名読み取り機
 */
public class TextSourceReader {

    JavaTextSourceReader javaTextSourceReader;
    KotlinTextSourceReader kotlinTextSourceReader;
    ScalaSourceAliasReader scalaSourceAliasReader;

    public TextSourceReader(JavaTextSourceReader javaTextSourceReader) {
        this(javaTextSourceReader, sources -> ClassAndMethodComments.empty(), sources -> ClassAndMethodComments.empty());
    }

    public TextSourceReader(JavaTextSourceReader javaTextSourceReader, KotlinTextSourceReader kotlinTextSourceReader) {
        this(javaTextSourceReader, kotlinTextSourceReader, sources -> ClassAndMethodComments.empty());
    }

    public TextSourceReader(JavaTextSourceReader javaTextSourceReader, ScalaSourceAliasReader scalaSourceAliasReader) {
        this(javaTextSourceReader, sources -> ClassAndMethodComments.empty(), scalaSourceAliasReader);
    }

    private TextSourceReader(JavaTextSourceReader javaTextSourceReader, KotlinTextSourceReader kotlinTextSourceReader, ScalaSourceAliasReader scalaSourceAliasReader) {
        this.javaTextSourceReader = javaTextSourceReader;
        this.kotlinTextSourceReader = kotlinTextSourceReader;
        this.scalaSourceAliasReader = scalaSourceAliasReader;
    }

    public PackageComments readPackageComments(CodeSources codeSources) {
        return javaTextSourceReader.readPackages(codeSources.packageInfoSources());
    }

    public ClassAndMethodComments readClassAndMethodComments(CodeSources codeSources) {
        return Stream.of(
                javaTextSourceReader.readClasses(codeSources.javaSources()),
                kotlinTextSourceReader.readClasses(codeSources.kotlinSources()),
                scalaSourceAliasReader.readAlias(codeSources.scalaSources()))
                .reduce(ClassAndMethodComments.empty(), ClassAndMethodComments::merge);
    }
}
