package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionsTest {

    @Test
    void インストラクションなし() {
        var instructions = new Instructions(List.of());

        assertEquals(0, instructions.methodCallStream().count());
        assertEquals(0, instructions.lambdaInlinedMethodCallStream().count());
    }

    @Test
    void 通常のメソッド呼び出し() {
        var instructions = new Instructions(List.of(
                new MethodCall(TypeId.valueOf("MyClass"), "a", List.of(), TypeId.valueOf("void")),
                new MethodCall(TypeId.valueOf("MyClass"), "b", List.of(), TypeId.valueOf("void"))
        ));

        assertEquals(2, instructions.methodCallStream().count());
        assertEquals(2, instructions.lambdaInlinedMethodCallStream().count());
    }

    @Test
    void 動的メソッド呼び出し() {
        var instructions = new Instructions(List.of(
                new DynamicMethodCall(null, null, null),
                new DynamicMethodCall(null, null, null)
        ));

        assertEquals(2, instructions.methodCallStream().count());
        assertEquals(2, instructions.lambdaInlinedMethodCallStream().count());
    }

    @Test
    void lambda() {
        var lambdaInstruction1 = new LambdaExpressionCall(new DynamicMethodCall(null, null, null),
                new Instructions(List.of(
                        new MethodCall(TypeId.valueOf("MyClass"), "a", List.of(), TypeId.valueOf("void")),
                        new MethodCall(TypeId.valueOf("MyClass"), "b", List.of(), TypeId.valueOf("void"))
                )));
        var lambdaInstruction2 = new LambdaExpressionCall(new DynamicMethodCall(null, null, null),
                new Instructions(List.of(
                        new MethodCall(TypeId.valueOf("MyClass2"), "a", List.of(), TypeId.valueOf("void")),
                        new LambdaExpressionCall(new DynamicMethodCall(null, null, null),
                                new Instructions(List.of(
                                        new MethodCall(TypeId.valueOf("MyClass3"), "a", List.of(), TypeId.valueOf("void")),
                                        new MethodCall(TypeId.valueOf("MyClass3"), "b", List.of(), TypeId.valueOf("void")),
                                        new MethodCall(TypeId.valueOf("MyClass3"), "c", List.of(), TypeId.valueOf("void"))
                                )))
                )));


        var instructions = new Instructions(List.of(lambdaInstruction1, lambdaInstruction2));

        assertEquals(2, instructions.methodCallStream().count());
        // lambdaを展開してMethodCallを数える
        assertEquals(6, instructions.lambdaInlinedMethodCallStream().count());
    }
}