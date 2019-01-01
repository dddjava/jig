package org.dddjava.jig.domain.model.implementation.raw;

/**
 * 生ソース
 */
public class RawSource {

    TextSource textSource;
    BinarySource binarySource;

    public RawSource(TextSource textSource, BinarySource binarySource) {
        this.textSource = textSource;
        this.binarySource = binarySource;
    }

    public TextSource textSource() {
        return textSource;
    }

    public BinarySource binarySource() {
        return binarySource;
    }
}
