package org.dddjava.jig.domain.model.implementation.raw;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * package-infoソース
 */
public class PackageInfoSource {

    byte[] value;

    public PackageInfoSource(byte[] value) {
        this.value = value;
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(value);
    }
}
