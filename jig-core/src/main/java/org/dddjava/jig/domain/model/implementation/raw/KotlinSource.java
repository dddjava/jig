package org.dddjava.jig.domain.model.implementation.raw;

import org.dddjava.jig.domain.model.implementation.raw.sourcepath.SourceFilePath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * .ktソース
 */
public class KotlinSource implements SourceCode {

    SourceFilePath sourceFilePath;
    byte[] value;

    public KotlinSource(SourceFilePath sourceFilePath, byte[] value) {
        this.sourceFilePath = sourceFilePath;
        this.value = value;
    }

    @Override
    public SourceFilePath sourceFilePath() {
        return sourceFilePath;
    }

    @Override
    public InputStream toInputStream() {
        return new ByteArrayInputStream(value);
    }

    @Override
    public String toString() {
        return "KotlinSource[" + sourceFilePath + "]";
    }
}
