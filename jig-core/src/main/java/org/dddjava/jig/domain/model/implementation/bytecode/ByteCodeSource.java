package org.dddjava.jig.domain.model.implementation.bytecode;

/**
 * バイトコードのソース（classファイル）
 */
public class ByteCodeSource {
    private final byte[] value;

    public ByteCodeSource(byte[] value) {
        this.value = value;
    }

    public byte[] value() {
        return value;
    }
}
