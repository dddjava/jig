package org.dddjava.jig.domain.model.sources.classsources;

/**
 * classソース
 */
public class ClassSource {

    byte[] value;
    String className;

    public ClassSource(byte[] value, String className) {
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
                "value(byte[] length)=" + value.length +
                ", className='" + className + '\'' +
                '}';
    }
}
