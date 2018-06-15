package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * メソッド定義
 */
public class MethodDeclaration {

    private final MethodReturn methodReturn;

    MethodIdentifier methodIdentifier;

    public MethodDeclaration(TypeIdentifier declaringType, MethodSignature methodSignature, MethodReturn methodReturn) {
        this.methodReturn = methodReturn;

        this.methodIdentifier = new MethodIdentifier(declaringType, methodSignature);
    }

    public String asFullNameText() {
        return methodIdentifier.asText();
    }

    public String asSignatureSimpleText() {
        return methodSignature().asSimpleText();
    }

    public String asSignatureAndReturnTypeSimpleText() {
        return asSignatureSimpleText() + ":" + returnType().asSimpleText();
    }

    public TypeIdentifier declaringType() {
        return methodIdentifier.declaringType();
    }

    public MethodSignature methodSignature() {
        return methodIdentifier.methodSignature();
    }

    public MethodDeclaration with(TypeIdentifier typeIdentifier) {
        return new MethodDeclaration(typeIdentifier, methodSignature(), methodReturn);
    }

    public TypeIdentifier returnType() {
        return methodReturn.typeIdentifier;
    }

    public boolean isConstructor() {
        return methodSignature().isConstructor();
    }

    public boolean isLambda() {
        return methodSignature().isLambda();
    }

    String asSimpleTextWithDeclaringType() {
        return declaringType().asSimpleText() + "." + asSignatureSimpleText();
    }

    public boolean sameIdentifier(MethodDeclaration methodDeclaration) {
        return methodIdentifier.equals(methodDeclaration.methodIdentifier);
    }

    public boolean matches(TypeIdentifier typeIdentifier, String methodName) {
        return methodIdentifier.matches(typeIdentifier, methodName);
    }

    public MethodIdentifier identifier() {
        return methodIdentifier;
    }
}
