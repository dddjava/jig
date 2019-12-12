package org.dddjava.jig.domain.model.jigsource.source.code.javacode;

import java.io.InputStream;

/**
 * package-infoソース
 */
public class PackageInfoSource {

    JavaSource javaSource;

    public PackageInfoSource(JavaSource javaSource) {
        this.javaSource = javaSource;
    }

    public InputStream toInputStream() {
        return javaSource.toInputStream();
    }
}
