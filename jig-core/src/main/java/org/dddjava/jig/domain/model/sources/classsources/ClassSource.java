package org.dddjava.jig.domain.model.sources.classsources;

/**
 * classソース
 */
public class ClassSource {

    byte[] value;

    public ClassSource(byte[] value) {
        this.value = value;
    }

    public byte[] value() {
        return value;
    }

    @Override
    public String toString() {
        return "ClassSource{" +
                "value(byte[] length)=" + value.length +
                '}';
    }
}
