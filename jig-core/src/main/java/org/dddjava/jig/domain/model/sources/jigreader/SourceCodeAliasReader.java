package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;

/**
 * コードを使用する別名別名読み取り機
 */
public class SourceCodeAliasReader {

    JavaTextSourceReader javaTextSourceReader;
    KotlinTextSourceReader kotlinTextSourceReader;
    ScalaSourceAliasReader scalaSourceAliasReader;

    public SourceCodeAliasReader(JavaTextSourceReader javaTextSourceReader) {
        this(javaTextSourceReader, sources -> ClassAndMethodComments.empty(), sources -> ClassAndMethodComments.empty());
    }

    public SourceCodeAliasReader(JavaTextSourceReader javaTextSourceReader, KotlinTextSourceReader kotlinTextSourceReader) {
        this(javaTextSourceReader, kotlinTextSourceReader, sources -> ClassAndMethodComments.empty());
    }

    public SourceCodeAliasReader(JavaTextSourceReader javaTextSourceReader, ScalaSourceAliasReader scalaSourceAliasReader) {
        this(javaTextSourceReader, sources -> ClassAndMethodComments.empty(), scalaSourceAliasReader);
    }

    private SourceCodeAliasReader(JavaTextSourceReader javaTextSourceReader, KotlinTextSourceReader kotlinTextSourceReader, ScalaSourceAliasReader scalaSourceAliasReader) {
        this.javaTextSourceReader = javaTextSourceReader;
        this.kotlinTextSourceReader = kotlinTextSourceReader;
        this.scalaSourceAliasReader = scalaSourceAliasReader;
    }

    public PackageComments readPackages(PackageInfoSources packageInfoSources) {
        return javaTextSourceReader.readPackages(packageInfoSources);
    }

    public ClassAndMethodComments readJavaSources(JavaSources javaSources) {
        return javaTextSourceReader.readClasses(javaSources);
    }

    public ClassAndMethodComments readKotlinSources(KotlinSources kotlinSources) {
        return kotlinTextSourceReader.readClasses(kotlinSources);
    }

    public ClassAndMethodComments readScalaSources(ScalaSources scalaSources) {
        return scalaSourceAliasReader.readAlias(scalaSources);
    }
}
