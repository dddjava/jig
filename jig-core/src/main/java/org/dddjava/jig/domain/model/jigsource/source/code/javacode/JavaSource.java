package org.dddjava.jig.domain.model.jigsource.source.code.javacode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * *.javaソース
 */
public class JavaSource {

    JavaSourceFile javaSourceFile;
    byte[] value;

    public JavaSource(JavaSourceFile javaSourceFile, byte[] value) {
        this.javaSourceFile = javaSourceFile;
        this.value = value;
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(value);
    }

    @Override
    public String toString() {
        return "JavaSource[" + javaSourceFile + "]";
    }
}
