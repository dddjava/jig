package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メソッド呼び出し
 */
public record MethodCall(TypeIdentifier methodOwner, String methodName,
                         List<TypeIdentifier> argumentTypes,
                         TypeIdentifier returnType) implements Instruction {

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

    @Override
    public Stream<MethodCall> findMethodCall() {
        return Stream.of(this);
    }

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.concat(
                argumentTypes.stream(),
                Stream.of(methodOwner, returnType)
        );
    }

    public boolean isLambda() {
        return jigMethodIdentifier().isLambda();
    }
}
