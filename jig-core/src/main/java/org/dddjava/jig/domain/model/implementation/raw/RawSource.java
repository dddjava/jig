package org.dddjava.jig.domain.model.implementation.raw;

/**
 * 生ソース
 */
public class RawSource {

    TextSources textSources;
    BinarySources binarySources;

    public RawSource(TextSources textSources, BinarySources binarySources) {
        this.textSources = textSources;
        this.binarySources = binarySources;
    }

    public TextSource textSource() {
        return textSources.toTextSource();
    }

    public BinarySource binarySource() {
        return binarySources.toBinarySource();
    }
}
