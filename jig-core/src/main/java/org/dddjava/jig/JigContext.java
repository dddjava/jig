package org.dddjava.jig;

public enum JigContext {
    /**
     * パッケージの略し方
     * initial, numeric
     */
    packageAbbreviationMode("initial");

    private final String value;

    JigContext(String defaultValue) {
        this.value = defaultValue;
    }

    public String value() {
        return value;
    }
}
