package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 動的メソッド呼び出し
 *
 * メソッド参照の場合はmethodCallには対象のメソッドが入る。
 *
 * @param methodCall 呼び出すメソッド
 */
public record DynamicMethodCall(MethodCall methodCall, TypeId returnType,
                                List<TypeId> argumentTypes) implements Instruction {

    @Override
    public Stream<MethodCall> methodCallStream() {
        return Stream.of(methodCall());
    }

    @Override
    public Stream<TypeId> streamAssociatedTypes() {
        return Stream.of(
                methodCall().streamAssociatedTypes(),
                argumentTypes().stream(),
                Stream.of(returnType()).filter(Predicate.not(TypeId::isVoid))
        ).flatMap(stream -> stream);
    }
}
