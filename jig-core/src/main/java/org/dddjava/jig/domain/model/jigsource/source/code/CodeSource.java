package org.dddjava.jig.domain.model.jigsource.source.code;

import org.dddjava.jig.domain.model.jigsource.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.jigsource.source.code.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.jigsource.source.code.kotlincode.KotlinSources;

public class CodeSource {
    JavaSources javaSources;
    KotlinSources kotlinSources;
    PackageInfoSources packageInfoSources;

    public CodeSource(JavaSources javaSources, KotlinSources kotlinSources, PackageInfoSources packageInfoSources) {
        this.javaSources = javaSources;
        this.kotlinSources = kotlinSources;
        this.packageInfoSources = packageInfoSources;
    }
}
