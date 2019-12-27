package org.dddjava.jig.domain.model.jigsource.source.code;

import org.dddjava.jig.domain.model.jigsource.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.jigsource.source.code.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.jigsource.source.code.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.jigsource.source.code.scalacode.ScalaSources;

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
