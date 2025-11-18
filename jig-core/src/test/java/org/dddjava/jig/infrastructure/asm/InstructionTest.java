package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.junit.jupiter.api.Test;
import stub.domain.model.relation.method.*;
import testing.TestSupport;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * メソッド内の呼び出しと順番が保持されていることを検証する
 */
public class InstructionTest {

    @Test
    void メソッドの使用しているメソッドが取得できる_通常のメソッド呼び出し() {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "method");
        var methodCalls = jigMethod.usingMethods().methodCalls();

        assertEquals(List.of("invokeMethod", "chainedInvokeMethod"),
                methodCalls.stream().map(MethodCall::methodName).toList());
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_メソッド参照() {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "methodRef");
        var methodCalls = jigMethod.usingMethods().methodCalls();

        assertEquals(List.of("referenceMethod"),
                methodCalls.stream().map(MethodCall::methodName).toList());
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_lambda式() {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "lambda");
        var methodCalls = jigMethod.usingMethods().methodCalls();

        assertEquals(List.of("empty", "lambda$lambda$0", "forEach"),
                methodCalls.stream().map(MethodCall::methodName).toList());
    }

    private static class MethodInstructionTestStub {

        InstructionField instructionField = null;

        void method(MethodArgument methodArgument) throws Exception {
            // ローカル変数宣言だけ
            LocalValue localValue = null;

            // メソッド呼び出し
            instructionField.invokeMethod().chainedInvokeMethod();
        }

        void lambda() {
            Stream.empty()
                    .forEach(item -> {
                        // Lambdaの中でだけ使用しているクラス
                        new UseInLambda();
                    });
        }

        void methodRef() {
            // メソッド参照
            Function<MethodReference, String> method = MethodReference::referenceMethod;
        }
    }
}
