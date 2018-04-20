package jig.domain.model.definition.method;

import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.Objects;

public class MethodDefinition {

    private final TypeIdentifier declaringType;
    private final MethodSignature methodSignature;

    private final String fullText;

    public MethodDefinition(TypeIdentifier declaringType, MethodSignature methodSignature) {
        this.declaringType = declaringType;
        this.methodSignature = methodSignature;

        this.fullText = declaringType.fullQualifiedName() + "." + methodSignature.asText();
    }

    public String asFullText() {
        return fullText;
    }

    public String asSimpleText() {
        return methodSignature.asSimpleText();
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public MethodSignature methodSignature() {
        return methodSignature;
    }

    public MethodDefinition with(TypeIdentifier typeIdentifier) {
        return new MethodDefinition(typeIdentifier, this.methodSignature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDefinition that = (MethodDefinition) o;
        return Objects.equals(fullText, that.fullText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullText);
    }

}
