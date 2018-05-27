package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

import java.util.Objects;

public class MethodDeclaration {

    private final TypeIdentifier declaringType;
    private final MethodSignature methodSignature;
    private final TypeIdentifier returnTypeIdentifier;

    private final String fullText;

    public MethodDeclaration(TypeIdentifier declaringType, MethodSignature methodSignature, TypeIdentifier returnTypeIdentifier) {
        this.declaringType = declaringType;
        this.methodSignature = methodSignature;
        this.returnTypeIdentifier = returnTypeIdentifier;

        this.fullText = declaringType.fullQualifiedName() + "." + methodSignature.asText();
    }

    public String asFullText() {
        return fullText;
    }

    public String asSignatureSimpleText() {
        return methodSignature.asSimpleText();
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public MethodSignature methodSignature() {
        return methodSignature;
    }

    public MethodDeclaration with(TypeIdentifier typeIdentifier) {
        return new MethodDeclaration(typeIdentifier, this.methodSignature, returnTypeIdentifier);
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

    public String asSimpleTextWithReturnType() {
        return asSignatureSimpleText() + " : " + returnTypeIdentifier.asSimpleText();
    }

    public TypeIdentifier returnType() {
        return returnTypeIdentifier;
    }

    public boolean isConstructor() {
        // 名前以外の判別方法があればそれにしたい
        return asSignatureSimpleText().startsWith("<init>");
    }

    public boolean isLambda() {
        return methodSignature.isLambda();
    }

    String asSimpleTextWithDeclaringType() {
        return declaringType().asSimpleText() + "." + asSignatureSimpleText();
    }
}
