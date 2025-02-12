package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッド定義
 */
public class MethodDeclaration {

    private final MethodReturn methodReturn;
    private final JigMethodIdentifier jigMethodIdentifier;
    private final TypeIdentifier declaringType;
    private final MethodSignature methodSignature;

    public MethodDeclaration(TypeIdentifier declaringType, MethodSignature methodSignature, MethodReturn methodReturn) {
        this.jigMethodIdentifier = JigMethodIdentifier.from(declaringType, methodSignature.methodName(),
                methodSignature.arguments().stream().map(ParameterizedType::typeIdentifier).toList());
        this.methodReturn = methodReturn;
        this.declaringType = declaringType;
        this.methodSignature = methodSignature;
    }

    /**
     * @return "org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration#asFullNameText()"
     */
    public String asFullNameText() {
        return jigMethodIdentifier.value();
    }

    /**
     * パッケージを省略したシグネチャの文字列表現
     *
     * @return "methodName(ArgumentType)"
     */
    public String asSignatureSimpleText() {
        return methodSignature().asSimpleText();
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public MethodSignature methodSignature() {
        return methodSignature;
    }

    public MethodReturn methodReturn() {
        return methodReturn;
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

    public JigMethodIdentifier jigMethodIdentifier() {
        return jigMethodIdentifier;
    }
}
