package org.dddjava.jig.domain.model.implementation.source.code.javacode;

import org.dddjava.jig.domain.model.implementation.raw.SourceCode;
import org.dddjava.jig.domain.model.implementation.raw.sourcepath.SourceFilePath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * *.javaソース
 */
public class JavaSource implements SourceCode {

    SourceFilePath sourceFilePath;
    byte[] value;

    public JavaSource(SourceFilePath sourceFilePath, byte[] value) {
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
        return "JavaSource[" + sourceFilePath + "]";
    }
}
