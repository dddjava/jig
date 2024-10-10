package org.dddjava.jig.domain.model.parts.classes.method;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * メソッドシグネチャ
 */
public record MethodSignature(String methodName, Arguments arguments) {

    public MethodSignature(String methodName) {
        this(methodName, Arguments.empty());
    }

    public MethodSignature(String methodName, TypeIdentifier argument) {
        this(methodName, new Arguments(Collections.singletonList(argument)));
    }

    /**
     * メソッド文字列表現。引数型もFQNでになります。
     *
     * @return "methodName(java.lang.String)"
     * @see #asSimpleText()
     */
    public String asText() {
        return methodName + "(" + arguments.argumentsAsText() + ")";
    }

    public String methodName() {
        return methodName;
    }

    /**
     * 人がよく目にする形のメソッド文字列表現。
     * `asText()` との差は引数型のパッケージが省略されるところです。
     *
     * @return "methodName(ArgumentType)"
     */
    public String asSimpleText() {
        return methodName + "(" + arguments.argumentsAsSimpleText() + ")";
    }

    public List<TypeIdentifier> listArgumentTypeIdentifiers() {
        return arguments.typeIdentifiers().list();
    }

    public boolean isLambda() {
        return methodName.startsWith("lambda$");
    }

    public boolean isConstructor() {
        // 名前以外の判別方法があればそれにしたい
        return methodName.equals("<init>");
    }

    public boolean isSame(MethodSignature other) {
        return methodName.equals(other.methodName)
                && arguments.isSame(other.arguments);
    }

    static List<MethodSignature> objectMethods = Arrays.asList(
            new MethodSignature("equals", TypeIdentifier.of(Object.class)),
            new MethodSignature("hashCode"),
            new MethodSignature("toString"));

    public boolean isObjectMethod() {
        for (MethodSignature objectMethod : objectMethods) {
            if (objectMethod.isSame(this)) {
                return true;
            }
        }
        return false;
    }

    public String packageAbbreviationText() {
        return methodName + "(" + arguments.packageAbbreviationText() + ")";
    }
}
