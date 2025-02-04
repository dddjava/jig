package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッド戻り値
 */
public class MethodReturn {

    ParameterizedType parameterizedType;

    public MethodReturn(ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    /**
     * ジェネリクスが解決できない場合のファクトリ
     */
    public static MethodReturn fromTypeOnly(TypeIdentifier typeIdentifier) {
        return new MethodReturn(new ParameterizedType(typeIdentifier));
    }

    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }

    public TypeIdentifier typeIdentifier() {
        return parameterizedType.typeIdentifier();
    }

    public boolean isPrimitive() {
        return typeIdentifier().isPrimitive();
    }

    public boolean isBoolean() {
        return typeIdentifier().isBoolean();
    }

    public boolean isStream() {
        return typeIdentifier().isStream();
    }

    public boolean isVoid() {
        return typeIdentifier().isVoid();
    }

    public String asSimpleText() {
        return parameterizedType.asSimpleText();
    }
}
