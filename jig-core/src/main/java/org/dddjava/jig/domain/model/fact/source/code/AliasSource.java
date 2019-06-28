package org.dddjava.jig.domain.model.fact.source.code;

import org.dddjava.jig.domain.model.fact.source.code.javacode.JavaSource;
import org.dddjava.jig.domain.model.fact.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.fact.source.code.javacode.PackageInfoSource;
import org.dddjava.jig.domain.model.fact.source.code.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.fact.source.code.kotlincode.KotlinSource;
import org.dddjava.jig.domain.model.fact.source.code.kotlincode.KotlinSources;

import java.util.ArrayList;
import java.util.List;

/**
 * 別名の情報源
 */
public class AliasSource {

    JavaSources javaSources;
    KotlinSources kotlinSources;
    PackageInfoSources packageInfoSources;

    public AliasSource(CodeSource codeSource) {
        this(codeSource.javaSources, codeSource.kotlinSources, codeSource.packageInfoSources);
    }

    public AliasSource(JavaSources javaSources, KotlinSources kotlinSources, PackageInfoSources packageInfoSources) {
        this.javaSources = javaSources;
        this.kotlinSources = kotlinSources;
        this.packageInfoSources = packageInfoSources;
    }

    public AliasSource() {
        this(new JavaSources(), new KotlinSources(), new PackageInfoSources());
    }

    public JavaSources javaSources() {
        return javaSources;
    }

    public KotlinSources kotlinSources() {
        return kotlinSources;
    }

    public PackageInfoSources packageInfoSources() {
        return packageInfoSources;
    }

    public AliasSource merge(AliasSource other) {
        List<JavaSource> javaSources = new ArrayList<>(this.javaSources.list());
        javaSources.addAll(other.javaSources.list());
        List<KotlinSource> kotlinSources = new ArrayList<>(this.kotlinSources.list());
        kotlinSources.addAll(other.kotlinSources.list());
        List<PackageInfoSource> packageInfoSources = new ArrayList<>(this.packageInfoSources.list());
        packageInfoSources.addAll(other.packageInfoSources.list());
        return new AliasSource(new JavaSources(javaSources), new KotlinSources(kotlinSources), new PackageInfoSources(packageInfoSources));
    }
}
