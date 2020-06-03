package org.dddjava.jig.domain.model.jigsource.file.binary;

/**
 * classソース
 */
public class ClassSource {

    BinarySourceLocation binarySourceLocation;
    byte[] value;
    String className;

    public ClassSource(BinarySourceLocation binarySourceLocation, byte[] value, String className) {
        this.binarySourceLocation = binarySourceLocation;
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
