package org.dddjava.jig.domain.model.parts.declaration.method;

/**
 * メソッドの可視性
 */
public enum Visibility {
    PUBLIC("+"),
    PROTECTED("#"),
    PACKAGE("~"),
    PRIVATE("-"),
    NOT_PUBLIC("");

    final String symbol;

    Visibility(String symbol) {
        this.symbol = symbol;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public String symbol() {
        return symbol;
    }
}
