package org.dddjava.jig.domain.model.data.types;

public record JigTypeArgument(String value) {
    public String simpleName() {
        int lastDotIndex = value.lastIndexOf('.');
        return (lastDotIndex != -1) ? value.substring(lastDotIndex + 1) : value;
    }

    public boolean notObject() {
        return !"java.lang.Object".equals(value);
    }
}
