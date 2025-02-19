package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.junit.jupiter.api.Test;
import stub.domain.model.relation.MethodInstructionTestStub;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstructionTest {

    @Test
    void メソッドの使用しているメソッドが取得できる_通常のメソッド呼び出し() throws Exception {
        var jigMethod = jigMethod("method(MethodArgument)");
        assertEquals(
                "[InstructionField.invokeMethod(), UsedInstructionMethodReturn.chainedInvokeMethod()]",
                jigMethod.usingMethods().asSimpleTextSorted()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_メソッド参照() throws Exception {
        var jigMethod = jigMethod("methodRef()");
        assertEquals(
                "[MethodReference.referenceMethod()]",
                jigMethod.usingMethods().asSimpleTextSorted()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_lambda式() throws Exception {
        var jigMethod = jigMethod("lambda()");
        assertEquals(
                "[MethodInstructionTestStub.lambda$lambda$0(Object), Stream.empty(), Stream.forEach(Consumer)]",
                jigMethod.usingMethods().asSimpleTextSorted()
        );

    }

    private static JigMethod jigMethod(String anObject) {
        var jigType = TestSupport.buildJigType(MethodInstructionTestStub.class);
        return jigType.instanceJigMethodStream()
                .filter(jigMethod -> jigMethod.nameAndArgumentSimpleText().equals(anObject))
                .findAny().orElseThrow();
    }
}
