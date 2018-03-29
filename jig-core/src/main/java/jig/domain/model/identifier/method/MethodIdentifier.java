package jig.domain.model.identifier.method;

import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.Objects;

public class MethodIdentifier {

    private final TypeIdentifier typeIdentifier;
    private final MethodSignature methodSignature;

    private final String fullText;

    public MethodIdentifier(TypeIdentifier typeIdentifier, MethodSignature methodSignature) {
        this.typeIdentifier = typeIdentifier;
        this.methodSignature = methodSignature;

        this.fullText = typeIdentifier.fullQualifiedName() + "." + methodSignature.asText();
    }

    public String asFullText() {
        return fullText;
    }

    public String asSimpleText() {
        return methodSignature.asSimpleText();
    }

    public TypeIdentifier declaringType() {
        return typeIdentifier;
    }

    public MethodSignature methodSignature() {
        return methodSignature;
    }

    public MethodIdentifier with(TypeIdentifier typeIdentifier) {
        return new MethodIdentifier(typeIdentifier, this.methodSignature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodIdentifier that = (MethodIdentifier) o;
        return Objects.equals(fullText, that.fullText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullText);
    }

}
