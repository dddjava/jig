package org.dddjava.jig.domain.model.parts.classes.method;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.Objects;

/**
 * メソッドの識別子
 */
public class MethodIdentifier {

    private final TypeIdentifier declaringType;
    private final MethodSignature methodSignature;

    public MethodIdentifier(TypeIdentifier declaringType, MethodSignature methodSignature) {
        this.declaringType = declaringType;
        this.methodSignature = methodSignature;
    }

    public String asText() {
        return declaringType.fullQualifiedName() + "." + methodSignature.asText();
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

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public MethodSignature methodSignature() {
        return methodSignature;
    }

    @Override
    public String toString() {
        return "MethodIdentifier{" + asText() + '}';
    }
}
