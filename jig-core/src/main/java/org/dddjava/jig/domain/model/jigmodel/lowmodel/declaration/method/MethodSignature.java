package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * メソッドシグネチャ
 */
public class MethodSignature {

    private final String methodName;

    Arguments arguments;

    public MethodSignature(String methodName, Arguments arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public String asText() {
        return methodName + "(" + arguments.argumentsAsText() + ")";
    }

    public String methodName() {
        return methodName;
    }

    public String asSimpleText() {
        return methodName + "(" + arguments.argumentsAsSimpleText() + ")";
    }

    public List<TypeIdentifier> arguments() {
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
            new MethodSignature("equals", new Arguments(Collections.singletonList(new TypeIdentifier(Object.class)))),
            new MethodSignature("hashCode", Arguments.empty()),
            new MethodSignature("toString", Arguments.empty()));

    public boolean isObjectMethod() {
        for (MethodSignature objectMethod : objectMethods) {
            if (objectMethod.isSame(this)) {
                return true;
            }
        }
        return false;
    }
}
