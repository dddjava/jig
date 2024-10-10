package org.dddjava.jig.domain.model.parts.classes.method;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * @return "org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration#asFullNameText()"
     */
    public String asFullNameText() {
        return methodIdentifier.asText();
    }

    /**
     * パッケージを省略したシグネチャの文字列表現
     *
     * @return "methodName(ArgumentType)"
     */
    public String asSignatureSimpleText() {
        return methodSignature().asSimpleText();
    }

    /**
     * @return "methodName(ArgumentType):ReturnType"
     */
    public String asSignatureAndReturnTypeSimpleText() {
        return asSignatureSimpleText() + ":" + methodReturn().asSimpleText();
    }

    public TypeIdentifier declaringType() {
        return methodIdentifier.declaringType();
    }

    public MethodSignature methodSignature() {
        return methodIdentifier.methodSignature();
    }

    public MethodReturn methodReturn() {
        return methodReturn;
    }

    public boolean isConstructor() {
        return methodSignature().isConstructor();
    }

    public boolean isLambda() {
        return methodSignature().isLambda();
    }

    /**
     * @return "ClassName.methodName(ArgumentType)"
     */
    public String asSimpleTextWithDeclaringType() {
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

    /**
     * パッケージおよび引数を省略した文字列表現。
     * オーバーロードされる場合は使用しづらいものになります。
     *
     * @return "ClassName.methodName"
     */
    public String asSimpleText() {
        return methodIdentifier.asSimpleText();
    }

    public TypeIdentifiers relateTypes() {
        ArrayList<TypeIdentifier> types = new ArrayList<>();
        types.add(methodReturn().typeIdentifier());
        types.addAll(methodSignature().arguments());
        return new TypeIdentifiers(types);
    }

    List<TypeIdentifier> argumentsTypeIdentifiers() {
        return methodSignature().arguments();
    }

    public String htmlIdText() {
        return identifier().htmlIdText();
    }
}
