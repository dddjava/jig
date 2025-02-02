package org.dddjava.jig.infrastructure.asm.data;

public record JigTypeArgument(String value) {
    public String simpleName() {
        int lastDotIndex = value.lastIndexOf('.');
        return (lastDotIndex != -1) ? value.substring(lastDotIndex + 1) : value;
    }
}
