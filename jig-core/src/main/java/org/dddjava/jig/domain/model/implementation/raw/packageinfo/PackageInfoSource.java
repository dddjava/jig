package org.dddjava.jig.domain.model.implementation.raw.packageinfo;

import org.dddjava.jig.domain.model.implementation.raw.javafile.JavaSource;

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
