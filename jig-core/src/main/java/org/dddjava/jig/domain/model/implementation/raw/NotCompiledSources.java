package org.dddjava.jig.domain.model.implementation.raw;

public class NotCompiledSources {
    JavaSources javaSources;
    PackageInfoSources packageInfoSources;

    public NotCompiledSources(JavaSources javaSources, PackageInfoSources packageInfoSources) {
        this.javaSources = javaSources;
        this.packageInfoSources = packageInfoSources;
    }

    public JavaSources javaSources() {
        return javaSources;
    }

    public PackageInfoSources packageInfoSources() {
        return packageInfoSources;
    }
}
