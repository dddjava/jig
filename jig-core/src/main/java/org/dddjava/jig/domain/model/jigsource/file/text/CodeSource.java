package org.dddjava.jig.domain.model.jigsource.file.text;

import org.dddjava.jig.domain.model.jigsource.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.jigsource.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.jigsource.file.text.scalacode.ScalaSources;

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
