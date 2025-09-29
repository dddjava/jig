package org.dddjava.jig.infrastructure.asm;

import org.junit.jupiter.api.Test;
import stub.domain.model.relation.method.*;
import testing.TestSupport;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * メソッド内の呼び出しと順番が保持されていることを検証する
 */
public class InstructionTest {

    @Test
    void メソッドの使用しているメソッドが取得できる_通常のメソッド呼び出し() throws Exception {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "method");
        var methodCalls = jigMethod.usingMethods().methodCalls();

        assertEquals(2, methodCalls.size());
        assertEquals("invokeMethod", methodCalls.get(0).methodName());
        assertEquals("chainedInvokeMethod", methodCalls.get(1).methodName());
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_メソッド参照() throws Exception {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "methodRef");
        var methodCalls = jigMethod.usingMethods().methodCalls();

        assertEquals(1, methodCalls.size());
        assertEquals("referenceMethod", methodCalls.get(0).methodName());
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_lambda式() throws Exception {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "lambda");
        var methodCalls = jigMethod.usingMethods().methodCalls();

        assertEquals(3, methodCalls.size());
        assertEquals("empty", methodCalls.get(0).methodName());
        // forEachに渡すLambdaが先に評価されるのでこの順番
        assertEquals("lambda$lambda$0", methodCalls.get(1).methodName());
        assertEquals("forEach", methodCalls.get(2).methodName());
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
