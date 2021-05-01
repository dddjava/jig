package org.dddjava.jig.domain.model.sources.file.text;

import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;

public class CodeSource {
    JavaSources javaSources;
    KotlinSources kotlinSources;
    ScalaSources scalaSources;
    PackageInfoSources packageInfoSources;

    public CodeSource(JavaSources javaSources, KotlinSources kotlinSources, ScalaSources scalaSources, PackageInfoSources packageInfoSources) {
        this.javaSources = javaSources;
        this.kotlinSources = kotlinSources;
        this.scalaSources = scalaSources;
        this.packageInfoSources = packageInfoSources;
    }
}
