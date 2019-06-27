package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.source.code.javacode.JavaSource;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.PackageInfoSource;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.implementation.source.code.kotlincode.KotlinSource;
import org.dddjava.jig.domain.model.implementation.source.code.kotlincode.KotlinSources;

import java.util.ArrayList;
import java.util.List;

/**
 * テキストソース
 */
public class AliasSource {

    JavaSources javaSources;
    KotlinSources kotlinSources;
    PackageInfoSources packageInfoSources;

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
