package org.dddjava.jig.infrastructure.asm.data;

public record JigObjectId<T>(String value) implements Comparable<JigObjectId<T>> {

    public String simpleValue() {
        if (!value.contains(".")) return value;
        return value.substring(value.lastIndexOf('.') + 1);
    }

    @Override
    public int compareTo(JigObjectId<T> o) {
        return this.value.compareTo(o.value);
    }
}
