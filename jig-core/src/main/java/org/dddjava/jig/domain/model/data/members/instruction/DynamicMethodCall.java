package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * 動的メソッド呼び出し
 *
 * メソッド参照の場合はmethodCallにはそのまま対象のメソッドが入る。
 * Lambda式の場合はlambda合成メソッドが入る。
 *
 * @param methodCall 呼び出すメソッド
 * @param returnType
 * @param argumentTypes
 */
public record DynamicMethodCall(MethodCall methodCall, TypeIdentifier returnType,
                                List<TypeIdentifier> argumentTypes) implements Instruction {

    @Override
    public Stream<MethodCall> findMethodCall() {
        return Stream.of(methodCall());
    }

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.of(
                methodCall().streamAssociatedTypes(),
                argumentTypes().stream(),
                // voidは返さなくていいと思うんだ
                Stream.of(returnType())
        ).flatMap(stream -> stream);
    }
}
