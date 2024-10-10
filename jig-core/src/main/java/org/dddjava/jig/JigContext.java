package org.dddjava.jig;

public enum JigContext {
    packageAbbreviationMode("initial");

    private final String value;

    JigContext(String defaultValue) {
        this.value = defaultValue;
    }

    public String value() {
        return value;
    }
}
