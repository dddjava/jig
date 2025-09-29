package org.dddjava.jig.infrastructure.asm;

import org.junit.jupiter.api.Test;
import stub.domain.model.relation.constant.to_primitive_constant.ConstantFieldHolder;
import stub.domain.model.relation.constant.to_primitive_wrapper_constant.IntegerConstantFieldHolder;
import stub.domain.model.relation.method.*;
import testing.TestSupport;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
                "[InstructionTest$MethodInstructionTestStub.lambda$lambda$0(Object), Stream.empty(), Stream.forEach(Consumer)]",
                jigMethod.usingMethods().asSimpleTextSorted()
        );

    }

    private static class MethodInstructionTestStub {

        InstructionField instructionField = null;

        @MethodAnnotation
        void method(MethodArgument methodArgument) throws Exception {
            // ローカル変数宣言だけ
            LocalValue localValue = null;

            // メソッド呼び出し
            instructionField.invokeMethod().chainedInvokeMethod();
        }

        void fieldRef() {
            // 別クラスのフィールドを参照する
            Object obj = ReferenceConstantOwnerInMethod.FIELD;
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

        MethodReturn method(List<ArgumentGenericsParameter> list) throws CheckedException {
            new Instantiation();
            return null;
        }

        void nestedClass() {
            new EnclosedClass.NestedClass();
        }

        void causeException() {
            throw new UncheckedExceptionA();
        }


        void accessPrimitiveConstantField() {
            Object obj = ConstantFieldHolder.INT_CONSTANT;
        }

        void accessStringConstantField() {
            Object obj = ConstantFieldHolder.STRING_CONSTANT;
        }

        void accessPrimitiveWrapperConstantField() {
            Object obj = IntegerConstantFieldHolder.INTEGER_CONSTANT;
        }
    }
}
