package org.dddjava.jig.infrastructure.asm;

import org.junit.jupiter.api.Test;
import stub.domain.model.relation.MethodInstructionTestStub;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstructionTest {

    @Test
    void メソッドの使用しているメソッドが取得できる_通常のメソッド呼び出し() throws Exception {
        var jigType = TestSupport.buildJigType(MethodInstructionTestStub.class);

        var list = jigType.instanceJigMethodStream()
                .filter(jigMethod -> jigMethod.nameAndArgumentSimpleText().equals("method(MethodArgument)"))
                .toList();
        assertEquals(
                "[InstructionField.invokeMethod(), UsedInstructionMethodReturn.chainedInvokeMethod()]",
                list.get(0).usingMethods().asSimpleTextSorted()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_メソッド参照() throws Exception {
        var jigType = TestSupport.buildJigType(MethodInstructionTestStub.class);

        var method3 = jigType.instanceJigMethodStream()
                .filter(jigMethod -> jigMethod.nameAndArgumentSimpleText().equals("methodRef()"))
                .toList();
        assertEquals(
                "[MethodReference.referenceMethod()]",
                method3.get(0).usingMethods().asSimpleTextSorted()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_lambda式() throws Exception {
        var jigType = TestSupport.buildJigType(MethodInstructionTestStub.class);

        var method2 = jigType.instanceJigMethodStream()
                .filter(jigMethod -> jigMethod.nameAndArgumentSimpleText().equals("lambda()"))
                .toList();
        assertEquals(
                "[MethodInstructionTestStub.lambda$lambda$0(Object), Stream.empty(), Stream.forEach(Consumer)]",
                method2.get(0).usingMethods().asSimpleTextSorted()
        );

    }
}
