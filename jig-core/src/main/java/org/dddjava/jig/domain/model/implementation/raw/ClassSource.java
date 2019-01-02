package org.dddjava.jig.domain.model.implementation.raw;

/**
 * classソース
 */
public class ClassSource {

    SourceLocation sourceLocation;
    byte[] value;
    String className;

    public ClassSource(SourceLocation sourceLocation, byte[] value, String className) {
        this.sourceLocation = sourceLocation;
        this.value = value;
        this.className = className;
    }

    public byte[] value() {
        return value;
    }

    public String className() {
        return className;
    }
}
