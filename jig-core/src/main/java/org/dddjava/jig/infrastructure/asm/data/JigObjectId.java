package org.dddjava.jig.infrastructure.asm.data;

public record JigObjectId<T>(String value) {

    public String simpleValue() {
        if (!value.contains(".")) return value;
        return value.substring(value.lastIndexOf('.') + 1);
    }
}
