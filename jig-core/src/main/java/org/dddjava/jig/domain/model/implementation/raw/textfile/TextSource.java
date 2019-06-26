package org.dddjava.jig.domain.model.implementation.raw.textfile;

import org.dddjava.jig.domain.model.implementation.raw.KotlinSource;
import org.dddjava.jig.domain.model.implementation.raw.KotlinSources;
import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSource;
import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSources;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSource;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSources;
import org.dddjava.jig.domain.model.implementation.raw.sourcelocation.SourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * テキストソース
 */
public class TextSource {

    SourceLocation sourceLocation;
    JavaSources javaSources;
    KotlinSources kotlinSources;
    PackageInfoSources packageInfoSources;

    public TextSource(SourceLocation sourceLocation, JavaSources javaSources, KotlinSources kotlinSources, PackageInfoSources packageInfoSources) {
        this.sourceLocation = sourceLocation;
        this.javaSources = javaSources;
        this.kotlinSources = kotlinSources;
        this.packageInfoSources = packageInfoSources;
    }

    public TextSource() {
        this(new SourceLocation(), new JavaSources(), new KotlinSources(), new PackageInfoSources());
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

    public TextSource merge(TextSource other) {
        List<JavaSource> javaSources = new ArrayList<>(this.javaSources.list());
        javaSources.addAll(other.javaSources.list());
        List<KotlinSource> kotlinSources = new ArrayList<>(this.kotlinSources.list());
        kotlinSources.addAll(other.kotlinSources.list());
        List<PackageInfoSource> packageInfoSources = new ArrayList<>(this.packageInfoSources.list());
        packageInfoSources.addAll(other.packageInfoSources.list());
        return new TextSource(new SourceLocation(""), new JavaSources(javaSources), new KotlinSources(kotlinSources), new PackageInfoSources(packageInfoSources));
    }
}
