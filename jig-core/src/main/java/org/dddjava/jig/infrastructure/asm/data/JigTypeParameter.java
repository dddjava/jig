package org.dddjava.jig.infrastructure.asm.data;

public record JigTypeParameter(String name) {
    public String simpleName() {
        int lastDotIndex = name.lastIndexOf('.');
        return (lastDotIndex != -1) ? name.substring(lastDotIndex + 1) : name;
    }
}
