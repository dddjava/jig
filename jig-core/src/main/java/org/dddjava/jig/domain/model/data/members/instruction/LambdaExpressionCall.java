package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * Lambda式呼び出し
 *
 * {@link DynamicMethodCall} の特化型で、呼び出されているLambdaの {@link Instructions} を保持する。
 * メソッドを辿らずともこれを展開すれば実装上の処理は追える。
 */
public record LambdaExpressionCall(MethodCall methodCall, TypeIdentifier returnType,
                                   List<TypeIdentifier> argumentTypes,
                                   Instructions instructions) implements Instruction {
    public static LambdaExpressionCall from(DynamicMethodCall origin, Instructions instructions) {
        return new LambdaExpressionCall(origin.methodCall(), origin.returnType(), origin.argumentTypes(), instructions);
    }

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
                Stream.of(returnType()),
                // lambdaの中身
                instructions.associatedTypeStream()
        ).flatMap(stream -> stream);
    }
}
