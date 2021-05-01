package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.package_.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;

/**
 * コードを使用する別名別名読み取り機
 */
public class SourceCodeAliasReader {

    JavaSourceAliasReader javaSourceAliasReader;
    KotlinSourceAliasReader kotlinSourceAliasReader;
    ScalaSourceAliasReader scalaSourceAliasReader;

    public SourceCodeAliasReader(JavaSourceAliasReader javaSourceAliasReader) {
        this(javaSourceAliasReader, sources -> ClassAndMethodComments.empty(), sources -> ClassAndMethodComments.empty());
    }

    public SourceCodeAliasReader(JavaSourceAliasReader javaSourceAliasReader, KotlinSourceAliasReader kotlinSourceAliasReader) {
        this(javaSourceAliasReader, kotlinSourceAliasReader, sources -> ClassAndMethodComments.empty());
    }

    public SourceCodeAliasReader(JavaSourceAliasReader javaSourceAliasReader, ScalaSourceAliasReader scalaSourceAliasReader) {
        this(javaSourceAliasReader, sources -> ClassAndMethodComments.empty(), scalaSourceAliasReader);
    }

    private SourceCodeAliasReader(JavaSourceAliasReader javaSourceAliasReader, KotlinSourceAliasReader kotlinSourceAliasReader, ScalaSourceAliasReader scalaSourceAliasReader) {
        this.javaSourceAliasReader = javaSourceAliasReader;
        this.kotlinSourceAliasReader = kotlinSourceAliasReader;
        this.scalaSourceAliasReader = scalaSourceAliasReader;
    }

    public PackageComments readPackages(PackageInfoSources packageInfoSources) {
        return javaSourceAliasReader.readPackages(packageInfoSources);
    }

    public ClassAndMethodComments readJavaSources(JavaSources javaSources) {
        return javaSourceAliasReader.readAlias(javaSources);
    }

    public ClassAndMethodComments readKotlinSources(KotlinSources kotlinSources) {
        return kotlinSourceAliasReader.readAlias(kotlinSources);
    }

    public ClassAndMethodComments readScalaSources(ScalaSources scalaSources) {
        return scalaSourceAliasReader.readAlias(scalaSources);
    }
}
