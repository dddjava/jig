package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メソッド呼び出し
 */
public record MethodCall(TypeId methodOwner, String methodName,
                         List<TypeId> argumentTypes,
                         TypeId returnType) implements Instruction {

    public boolean jigMethodIdIs(JigMethodId jigMethodId) {
        return jigMethodId.equals(jigMethodId());
    }

    public JigMethodId jigMethodId() {
        return JigMethodId.from(methodOwner, methodName, argumentTypes);
    }

    public String asSignatureAndReturnTypeSimpleText() {
        return "%s(%s):%s".formatted(methodName,
                argumentTypes.stream().map(TypeId::asSimpleText).collect(Collectors.joining(", ")),
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
    public Stream<TypeId> streamAssociatedTypes() {
        return Stream.concat(
                argumentTypes.stream(),
                Stream.of(methodOwner, returnType)
        ).filter(Predicate.not(TypeId::isVoid));
    }

    public boolean isLambda() {
        return jigMethodId().isLambda();
    }
}
