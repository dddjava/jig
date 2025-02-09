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
        return just(JigTypeReference.fromId(value));
    }

    public static JigTypeArgument just(JigTypeReference jigTypeReference) {
        return new JigTypeArgument(jigTypeReference, "=");
    }

    public String simpleName() {
        return jigTypeReference.simpleNameWithGenerics();
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

    public String fqn() {
        return jigTypeReference.fqnWithGenerics();
    }
}
