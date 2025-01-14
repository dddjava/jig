package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

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
                Objects.equals(methodSignature, that.methodSignature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, methodSignature);
    }

    public boolean possiblyMatches(MethodIdentifier methodIdentifier) {
        return declaringType.equals(methodIdentifier.declaringType) && methodIdentifier.methodSignature.methodName().equals(methodSignature.methodName());
    }

    @Override
    public String toString() {
        return "MethodIdentifier{" + asText() + '}';
    }

    public String htmlIdText() {
        // 英数字以外を_に置換する
        return packageAbbreviationText().replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String packageAbbreviationText() {
        return "%s.%s".formatted(
                declaringType.packageAbbreviationText(),
                methodSignature.packageAbbreviationText());
    }
}
