package org.dddjava.jig.domain.model.implementation.raw;

/**
 * classソース
 */
public class ClassSource {
    private final byte[] value;

    public ClassSource(byte[] value) {
        this.value = value;
    }

    public byte[] value() {
        return value;
    }
}
