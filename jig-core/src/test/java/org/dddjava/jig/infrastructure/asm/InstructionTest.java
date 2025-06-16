package org.dddjava.jig.infrastructure.asm;

import org.junit.jupiter.api.Test;
import stub.domain.model.relation.MethodInstructionTestStub;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstructionTest {

    @Test
    void メソッドの使用しているメソッドが取得できる_通常のメソッド呼び出し() throws Exception {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "method");
        assertEquals(
                "[InstructionField.invokeMethod(), UsedInstructionMethodReturn.chainedInvokeMethod()]",
                jigMethod.usingMethods().asSimpleTextSorted()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_メソッド参照() throws Exception {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "methodRef");
        assertEquals(
                "[MethodReference.referenceMethod()]",
                jigMethod.usingMethods().asSimpleTextSorted()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_lambda式() throws Exception {
        var jigMethod = TestSupport.JigMethod準備(MethodInstructionTestStub.class, "lambda");
        assertEquals(
                "[MethodInstructionTestStub.lambda$lambda$0(Object), Stream.empty(), Stream.forEach(Consumer)]",
                jigMethod.usingMethods().asSimpleTextSorted()
        );

    }
}
