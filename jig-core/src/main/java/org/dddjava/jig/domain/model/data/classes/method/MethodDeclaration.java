package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * メソッド定義
 */
public class MethodDeclaration {

    private final MethodReturn methodReturn;
    private final JigMethodIdentifier jigMethodIdentifier;
    private final TypeIdentifier declaringType;

    MethodIdentifier methodIdentifier;

    public MethodDeclaration(TypeIdentifier declaringType, MethodSignature methodSignature, MethodReturn methodReturn) {
        this.jigMethodIdentifier = JigMethodIdentifier.from(declaringType, methodSignature.methodName(),
                methodSignature.arguments().stream().map(ParameterizedType::typeIdentifier).toList());
        this.methodReturn = methodReturn;
        this.declaringType = declaringType;
        this.methodIdentifier = new MethodIdentifier(this.declaringType, methodSignature);
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

    /**
     * @return "methodName(ArgumentType):ReturnType"
     */
    public String asSignatureAndReturnTypeSimpleText() {
        return asSignatureSimpleText() + ":" + methodReturn().asSimpleText();
    }

    public TypeIdentifier declaringType() {
        return declaringType;
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

    public List<TypeIdentifier> relateTypes() {
        ArrayList<TypeIdentifier> types = new ArrayList<>();
        types.add(methodReturn().typeIdentifier());
        types.addAll(methodReturn().parameterizedType().typeParameters().list());
        types.addAll(methodSignature().arguments().stream().map(ParameterizedType::typeIdentifier).toList());
        // TODO add arguments type parameter
        return types;
    }

    public boolean isJSL() {
        return declaringType().isJavaLanguageType();
    }

    public JigMethodIdentifier jigMethodIdentifier() {
        return jigMethodIdentifier;
    }
}
