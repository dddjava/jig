package org.dddjava.jig.domain.model.declaration.method;

public class MethodNumber {
    int value;

    public MethodNumber(int value) {
        this.value = value;
    }

    public String asText() {
        return Integer.toString(value);
    }
}
