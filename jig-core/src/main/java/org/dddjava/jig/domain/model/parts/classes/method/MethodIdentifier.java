package org.dddjava.jig.domain.model.parts.classes.method;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.Objects;

/**
 * メソッドの識別子
 */
public record MethodIdentifier(TypeIdentifier declaringType, MethodSignature methodSignature) {

    /**
     * @return "org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier#methodName(java.lang.String)"
     */
    public String asText() {
        return declaringType.fullQualifiedName() + "#" + methodSignature.asText();
    }

    /**
     * @return "MethodIdentifier.methodName"
     */
    public String asSimpleText() {
        return declaringType().asSimpleText() + "." + methodSignature().methodName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodIdentifier that = (MethodIdentifier) o;
        return Objects.equals(declaringType, that.declaringType) &&
                Objects.equals(methodSignature.asText(), that.methodSignature.asText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, methodSignature.asText());
    }

    public boolean matches(TypeIdentifier typeIdentifier, String methodName) {
        return declaringType.equals(typeIdentifier) && methodName.equals(methodSignature.methodName());
    }

    public boolean matchesIgnoreOverload(MethodIdentifier methodIdentifier) {
        return matches(methodIdentifier.declaringType, methodIdentifier.methodSignature.methodName());
    }

    @Override
    public String toString() {
        return "MethodIdentifier{" + asText() + '}';
    }

    public String htmlIdText() {
        // 英数字以外を_に置換する
        return asText().replaceAll("[^a-zA-Z0-9]", "_");
    }
}
