package org.dddjava.jig.domain.model.declaration.type;

/**
 * パラメータ化された型
 *
 * 総称型 {@code Hoge<T>} に対する {@code Hoge<Fuga>} 。
 */
public class ParameterizedType {

    TypeIdentifier typeIdentifier;
    TypeParameters typeParameters;

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeParameters typeParameters) {
        this.typeIdentifier = typeIdentifier;
        this.typeParameters = typeParameters;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public TypeParameters typeParameters() {
        return typeParameters;
    }
}
