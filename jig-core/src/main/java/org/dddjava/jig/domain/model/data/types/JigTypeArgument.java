package org.dddjava.jig.domain.model.data.types;

/**
 * {@link JigTypeParameter 型パラメタ} に対する具体的な引数
 * @param value 一般的には {@link TypeIdentifier} になるが、型パラメタが入ることもある。
 */
public record JigTypeArgument(String value) {
    public String simpleName() {
        int lastDotIndex = value.lastIndexOf('.');
        return (lastDotIndex != -1) ? value.substring(lastDotIndex + 1) : value;
    }

    public boolean notObject() {
        return !"java.lang.Object".equals(value);
    }
}
