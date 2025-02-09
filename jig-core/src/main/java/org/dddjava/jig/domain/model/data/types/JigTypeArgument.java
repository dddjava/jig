package org.dddjava.jig.domain.model.data.types;

/**
 * {@link JigTypeParameter 型パラメタ} に対する具体的な引数
 *
 * @param value 一般的には {@link TypeIdentifier} になるが、型パラメタが入ることもある。
 */
public record JigTypeArgument(String value) {

    public static JigTypeArgument primitive(String value) {
        return new JigTypeArgument(value);
    }

    /**
     * ワイルドカードなし
     */
    public static JigTypeArgument just(String value) {
        return new JigTypeArgument(value);
    }

    public String simpleName() {
        int lastDotIndex = value.lastIndexOf('.');
        return (lastDotIndex != -1) ? value.substring(lastDotIndex + 1) : value;
    }

    public boolean notObject() {
        return !"java.lang.Object".equals(value);
    }

    public TypeIdentifier typeIdentifier() {
        return TypeIdentifier.valueOf(value);
    }
}
