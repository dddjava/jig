package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 呼び出すメソッド
 *
 * MethodDeclarationやMethodSignatureはジェネリクスを含む場合などもあるため、呼び出しでは使用できない。
 */
public record MethodCall(TypeIdentifier methodOwner, String methodName,
                         List<TypeIdentifier> argumentTypes,
                         TypeIdentifier returnType) implements Instruction {

    public List<TypeIdentifier> extractTypeIdentifiers() {
        List<TypeIdentifier> extractedTypes = new ArrayList<>(argumentTypes);
        extractedTypes.add(methodOwner);
        extractedTypes.add(returnType);
        return extractedTypes;
    }

    public boolean jigMethodIdentifierIs(JigMethodIdentifier jigMethodIdentifier) {
        return jigMethodIdentifier.equals(jigMethodIdentifier());
    }

    public JigMethodIdentifier jigMethodIdentifier() {
        return JigMethodIdentifier.from(methodOwner, methodName, argumentTypes);
    }

    public String asSignatureAndReturnTypeSimpleText() {
        return "%s(%s):%s".formatted(methodName,
                argumentTypes.stream().map(TypeIdentifier::asSimpleText).collect(Collectors.joining(", ")),
                returnType.asSimpleText());
    }

    public boolean isJSL() {
        return methodOwner.isJavaLanguageType();
    }

    public boolean isConstructor() {
        // 名前以外の判別方法があればそれにしたい
        return methodName.equals("<init>");
    }
}
