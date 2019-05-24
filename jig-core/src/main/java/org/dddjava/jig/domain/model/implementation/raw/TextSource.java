package org.dddjava.jig.domain.model.implementation.raw;

import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSource;
import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSources;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSource;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSources;

import java.util.ArrayList;
import java.util.List;

/**
 * テキストソース
 */
public class TextSource {

    SourceLocation sourceLocation;
    JavaSources javaSources;
    PackageInfoSources packageInfoSources;

    public TextSource(SourceLocation sourceLocation, JavaSources javaSources, PackageInfoSources packageInfoSources) {
        this.sourceLocation = sourceLocation;
        this.javaSources = javaSources;
        this.packageInfoSources = packageInfoSources;
    }

    public TextSource() {
        this(new SourceLocation(), new JavaSources(), new PackageInfoSources());
    }

    public JavaSources javaSources() {
        return javaSources;
    }

    public PackageInfoSources packageInfoSources() {
        return packageInfoSources;
    }

    public TextSource merge(TextSource other) {
        List<JavaSource> javaSources = new ArrayList<>(this.javaSources.list());
        javaSources.addAll(other.javaSources.list());
        List<PackageInfoSource> packageInfoSources = new ArrayList<>(this.packageInfoSources.list());
        packageInfoSources.addAll(other.packageInfoSources.list());
        return new TextSource(new SourceLocation(""), new JavaSources(javaSources), new PackageInfoSources(packageInfoSources));
    }
}
