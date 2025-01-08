package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * メソッドシグネチャ
 */
public final class MethodSignature {
    private final String methodName;
    private final Arguments arguments;

    public MethodSignature(String methodName, Arguments arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public MethodSignature(String methodName, TypeIdentifier... arguments) {
        this(methodName, new Arguments(List.of(arguments)));
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

    static List<MethodSignature> objectMethods;

    static {
        objectMethods = Arrays.asList(
                new MethodSignature("equals", TypeIdentifier.from(Object.class)),
                new MethodSignature("hashCode"),
                new MethodSignature("toString"));
    }

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

    public Arguments arguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MethodSignature) obj;
        return Objects.equals(this.methodName, that.methodName) &&
                Objects.equals(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, arguments);
    }

    @Override
    public String toString() {
        return "MethodSignature[" +
                "methodName=" + methodName + ", " +
                "arguments=" + arguments + ']';
    }

}
