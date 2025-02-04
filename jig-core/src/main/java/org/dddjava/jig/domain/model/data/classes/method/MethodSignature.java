package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * メソッドシグネチャ
 *
 * メソッドシグネチャはメソッド名と引数で構成され、戻り値型や定義されたクラスは含まない。
 * オーバーライドは継承関係にあるクラスで同じシグネチャで定義されること。
 * オーバーロードは同じクラスに同じ名前の異なるシグネチャで定義されること。
 */
public final class MethodSignature {
    private final String methodName;
    private final List<ParameterizedType> arguments;

    public MethodSignature(String methodName, List<ParameterizedType> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public MethodSignature(String methodName, TypeIdentifier... arguments) {
        this(methodName, Stream.of(arguments).map(ParameterizedType::new).toList());
    }

    public static MethodSignature from(String methodName, List<ParameterizedType> arguments) {
        return new MethodSignature(methodName, arguments);
    }

    /**
     * メソッド文字列表現。引数型もFQNでになります。
     *
     * @return "methodName(java.lang.String)"
     * @see #asSimpleText()
     */
    public String asText() {
        var argumentsText = arguments.stream().map(ParameterizedType::asFullNameText).collect(joining(", "));
        return methodName + "(" + argumentsText + ")";
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
        var argumentsText = arguments.stream().map(ParameterizedType::asSimpleText).collect(joining(", "));
        return methodName + "(" + argumentsText + ")";
    }

    public List<ParameterizedType> arguments() {
        return arguments;
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
                && arguments.equals(other.arguments);
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
        var argumentsText = arguments.stream().map(ParameterizedType::typeIdentifier).map(TypeIdentifier::packageAbbreviationText).collect(joining(", "));
        return methodName + "(" + argumentsText + ")";
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
