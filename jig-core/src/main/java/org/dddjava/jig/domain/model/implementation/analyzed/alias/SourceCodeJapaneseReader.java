package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.implementation.source.code.kotlincode.KotlinSources;

public class SourceCodeJapaneseReader {

    JavaSourceAliasReader javaSourceAliasReader;
    KotlinSourceAliasReader kotlinSourceAliasReader;

    public SourceCodeJapaneseReader(JavaSourceAliasReader javaSourceAliasReader) {
        this(javaSourceAliasReader, sources -> TypeAliases.empty());
    }

    public SourceCodeJapaneseReader(JavaSourceAliasReader javaSourceAliasReader, KotlinSourceAliasReader kotlinSourceAliasReader) {
        this.javaSourceAliasReader = javaSourceAliasReader;
        this.kotlinSourceAliasReader = kotlinSourceAliasReader;
    }

    public PackageAliases readPackages(PackageInfoSources packageInfoSources) {
        return javaSourceAliasReader.readPackages(packageInfoSources);
    }

    public TypeAliases readJavaSources(JavaSources javaSources) {
        return javaSourceAliasReader.readAlias(javaSources);
    }

    public TypeAliases readKotlinSources(KotlinSources kotlinSources) {
        return kotlinSourceAliasReader.readAlias(kotlinSources);
    }
}
