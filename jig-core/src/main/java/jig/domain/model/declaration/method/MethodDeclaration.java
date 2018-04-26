package jig.domain.model.declaration.method;

import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.Objects;

public class MethodDeclaration {

    private final TypeIdentifier declaringType;
    private final MethodSignature methodSignature;

    private final String fullText;

    public MethodDeclaration(TypeIdentifier declaringType, MethodSignature methodSignature) {
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

    public MethodDeclaration with(TypeIdentifier typeIdentifier) {
        return new MethodDeclaration(typeIdentifier, this.methodSignature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDeclaration that = (MethodDeclaration) o;
        return Objects.equals(fullText, that.fullText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullText);
    }

    public String asSimpleTextWith(TypeIdentifier returnTypeIdentifier) {
        // TODO ReturnTypeはこの中に持ってしまいたい
        return asSimpleText() + " : " + returnTypeIdentifier.asSimpleText();
    }
}
