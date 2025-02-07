package org.dddjava.jig.domain.model.data.classes.method;

/**
 * メンバの可視性
 */
public enum JigMemberVisibility {
    PUBLIC("+"),
    PROTECTED("#"),
    PACKAGE("~"),
    PRIVATE("-");

    final String symbol;

    JigMemberVisibility(String symbol) {
        this.symbol = symbol;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public String symbol() {
        return symbol;
    }
}
