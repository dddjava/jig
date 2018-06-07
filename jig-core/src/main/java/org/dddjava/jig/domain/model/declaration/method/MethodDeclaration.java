package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.Objects;

/**
 * メソッド定義
 */
public class MethodDeclaration {

    private final TypeIdentifier declaringType;
    private final MethodSignature methodSignature;
    private final MethodReturn methodReturn;

    MethodIdentifier methodIdentifier;

    public MethodDeclaration(TypeIdentifier declaringType, MethodSignature methodSignature, MethodReturn methodReturn) {
        this.declaringType = declaringType;
        this.methodSignature = methodSignature;
        this.methodReturn = methodReturn;

        this.methodIdentifier = new MethodIdentifier(declaringType, methodSignature);
    }

    public String asFullText() {
        return methodIdentifier.asText();
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
        return new MethodDeclaration(typeIdentifier, this.methodSignature, methodReturn);
    }

    // TODO なくす
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDeclaration that = (MethodDeclaration) o;
        return Objects.equals(methodIdentifier, that.methodIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodIdentifier);
    }

    public TypeIdentifier returnType() {
        return methodReturn.typeIdentifier;
    }

    public boolean isConstructor() {
        return methodSignature.isConstructor();
    }

    public boolean isLambda() {
        return methodSignature.isLambda();
    }

    String asSimpleTextWithDeclaringType() {
        return declaringType().asSimpleText() + "." + asSignatureSimpleText();
    }
}
