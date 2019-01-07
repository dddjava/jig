package org.dddjava.jig.domain.model.implementation.raw;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * *.javaソース
 */
public class JavaSource {

    byte[] value;

    public JavaSource(byte[] value) {
        this.value = value;
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(value);
    }
}
