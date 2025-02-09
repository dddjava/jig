package org.dddjava.jig.domain.model.data.types;

/**
 * {@link JigTypeParameter 型パラメタ} に対する具体的な引数
 *
 * TODO wildcard対応
 */
public record JigTypeArgument(JigTypeReference jigTypeReference, String wildcard) {

    public static JigTypeArgument primitive(String value) {
        return new JigTypeArgument(JigTypeReference.fromId(value), "=");
    }

    /**
     * ワイルドカードなし
     */
    public static JigTypeArgument just(String value) {
        return new JigTypeArgument(JigTypeReference.fromId(value), "=");
    }

    public String simpleName() {
        int lastDotIndex = value().lastIndexOf('.');
        return (lastDotIndex != -1) ? value().substring(lastDotIndex + 1) : value();
    }

    public boolean notObject() {
        return !"java.lang.Object".equals(value());
    }

    public TypeIdentifier typeIdentifier() {
        return TypeIdentifier.valueOf(value());
    }

    public String value() {
        return jigTypeReference.id().value();
    }
}
