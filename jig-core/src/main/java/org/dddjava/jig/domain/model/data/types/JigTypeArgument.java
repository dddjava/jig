package org.dddjava.jig.domain.model.data.types;

/**
 * 型引数
 *
 * {@link JigTypeParameter 型パラメタ} に対する、具体的な引数。
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
        return just(JigTypeReference.fromId(value));
    }

    public static JigTypeArgument just(JigTypeReference jigTypeReference) {
        return new JigTypeArgument(jigTypeReference, "=");
    }

    public String simpleNameWithGenerics() {
        return jigTypeReference.simpleNameWithGenerics();
    }

    public boolean notObject() {
        return !"java.lang.Object".equals(value());
    }

    public TypeId typeIdentifier() {
        return TypeId.valueOf(value());
    }

    public String value() {
        return jigTypeReference.id().value();
    }

    public String fqnWithGenerics() {
        return jigTypeReference.fqnWithGenerics();
    }
}
