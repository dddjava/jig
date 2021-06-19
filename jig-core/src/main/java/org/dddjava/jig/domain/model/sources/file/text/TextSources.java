package org.dddjava.jig.domain.model.sources.file.text;

import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSource;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSource;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSource;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSource;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;

import java.util.ArrayList;
import java.util.List;

/**
 * 別名の情報源
 */
public class TextSources {

    JavaSources javaSources;
    KotlinSources kotlinSources;
    ScalaSources scalaSources;
    PackageInfoSources packageInfoSources;

    public TextSources(CodeSource codeSource) {
        this(codeSource.javaSources, codeSource.kotlinSources, codeSource.scalaSources, codeSource.packageInfoSources);
    }

    public TextSources(JavaSources javaSources, KotlinSources kotlinSources, ScalaSources scalaSources, PackageInfoSources packageInfoSources) {
        this.javaSources = javaSources;
        this.kotlinSources = kotlinSources;
        this.scalaSources = scalaSources;
        this.packageInfoSources = packageInfoSources;
    }

    public TextSources() {
        this(new JavaSources(), new KotlinSources(), new ScalaSources(), new PackageInfoSources());
    }

    public JavaSources javaSources() {
        return javaSources;
    }

    public KotlinSources kotlinSources() {
        return kotlinSources;
    }

    public ScalaSources scalaSources() {
        return scalaSources;
    }

    public PackageInfoSources packageInfoSources() {
        return packageInfoSources;
    }

    public TextSources merge(TextSources other) {
        List<JavaSource> javaSources = new ArrayList<>(this.javaSources.list());
        javaSources.addAll(other.javaSources.list());
        List<KotlinSource> kotlinSources = new ArrayList<>(this.kotlinSources.list());
        kotlinSources.addAll(other.kotlinSources.list());
        List<ScalaSource> scalaSources = new ArrayList<>(this.scalaSources.list());
        scalaSources.addAll(other.scalaSources.list());
        List<PackageInfoSource> packageInfoSources = new ArrayList<>(this.packageInfoSources.list());
        packageInfoSources.addAll(other.packageInfoSources.list());
        return new TextSources(new JavaSources(javaSources), new KotlinSources(kotlinSources), new ScalaSources(scalaSources), new PackageInfoSources(packageInfoSources));
    }
}
