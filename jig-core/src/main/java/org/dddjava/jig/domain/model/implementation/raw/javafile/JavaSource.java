package org.dddjava.jig.domain.model.implementation.raw.javafile;

import org.dddjava.jig.domain.model.implementation.raw.SourceFilePath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * *.javaソース
 */
public class JavaSource {

    SourceFilePath sourceFilePath;
    byte[] value;

    public JavaSource(SourceFilePath sourceFilePath, byte[] value) {
        this.sourceFilePath = sourceFilePath;
        this.value = value;
    }

    public SourceFilePath sourceFilePath() {
        return sourceFilePath;
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(value);
    }

    @Override
    public String toString() {
        return "JavaSource[" + sourceFilePath + "]";
    }
}
