package org.dddjava.jig.domain.model.data.types;

public record JigObjectId<T>(String value) implements Comparable<JigObjectId<T>> {

    public static JigObjectId<JigTypeHeader> fromJvmBinaryName(String jvmBinaryName) {
        return new JigObjectId<>(jvmBinaryName.replace('/', '.').replace('$', '.'));
    }

    public String simpleValue() {
        int lastDotIndex = value.lastIndexOf('.');
        return (lastDotIndex != -1) ? value.substring(lastDotIndex + 1) : value;
    }

    @Override
    public int compareTo(JigObjectId<T> o) {
        return this.value.compareTo(o.value);
    }
}
