package org.dddjava.jig.domain.model.implementation.raw;

/**
 * テキストソース
 */
public class TextSource {

    JavaSources javaSources;
    PackageInfoSources packageInfoSources;

    public TextSource(JavaSources javaSources, PackageInfoSources packageInfoSources) {
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
