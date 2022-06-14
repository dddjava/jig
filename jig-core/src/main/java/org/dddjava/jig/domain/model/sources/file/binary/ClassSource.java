package org.dddjava.jig.domain.model.sources.file.binary;

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

    @Override
    public String toString() {
        return "ClassSource{" +
                "binarySourceLocation=" + binarySourceLocation +
                ", value(byte[] length)=" + value.length +
                ", className='" + className + '\'' +
                '}';
    }
}
