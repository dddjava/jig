package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Lambda式呼び出し
 *
 * {@link DynamicMethodCall} の特化型で、呼び出されているLambdaの {@link Instructions} を保持する。
 * originのmethodCallはlambda式のコンパイルにより生成される合成メソッド。実装依存だが `lambda$methodName$0` のような名前になる。
 */
public record LambdaExpressionCall(DynamicMethodCall origin,
                                   Instructions lambdaExpressionInstructions) implements Instruction {
    public static LambdaExpressionCall from(DynamicMethodCall origin, Instructions instructions) {
        return new LambdaExpressionCall(origin, instructions);
    }

    @Override
    public Stream<MethodCall> findMethodCall() {
        return Stream.of(origin.methodCall());
    }

    @Override
    public Stream<MethodCall> lambdaInlinedMethodCallStream() {
        // methodCallは合成メソッドなので使用しない
        return lambdaExpressionInstructions.lambdaInlinedMethodCallStream();
    }

    @Override
    public Stream<TypeId> streamAssociatedTypes() {
        return Stream.of(
                origin.methodCall().streamAssociatedTypes(),
                origin.argumentTypes().stream(),
                Stream.of(origin.returnType()).filter(Predicate.not(TypeId::isVoid)),
                // lambdaの中身
                lambdaExpressionInstructions.associatedTypeStream()
        ).flatMap(stream -> stream);
    }
}
