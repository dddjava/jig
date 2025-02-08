package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import stub.domain.model.relation.ConstructorInstruction;
import stub.domain.model.relation.MethodInstructionTestStub;
import stub.domain.model.relation.StaticMethodInstruction;
import stub.domain.model.relation.constant.to_primitive_wrapper_constant.IntegerConstantFieldHolder;
import stub.domain.model.relation.method.*;
import testing.TestSupport;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstructionTest {

    @ParameterizedTest
    @ValueSource(classes = {MethodInstructionTestStub.class, StaticMethodInstruction.class, ConstructorInstruction.class})
    void メソッドで使用している型が取得できる(Class<?> clz) throws Exception {
        var jigType = TestSupport.buildJigType(clz);

        TypeIdentifiers identifiers = jigType.usingTypes();
        assertThat(identifiers.list())
                .containsExactlyInAnyOrder(
                        // 標準
                        TypeIdentifier.from(Object.class),
                        TypeIdentifier.from(String.class),
                        TypeIdentifier.from(List.class),
                        TypeIdentifier.from(Stream.class),
                        TypeIdentifier.valueOf("void"),
                        TypeIdentifier.from(Exception.class),
                        // 自身への参照（コンストラクタ？）
                        TypeIdentifier.from(clz),
                        // メソッド定義
                        TypeIdentifier.from(MethodAnnotation.class),
                        TypeIdentifier.from(MethodArgument.class),
                        TypeIdentifier.from(MethodReturn.class),
                        TypeIdentifier.from(ArgumentGenericsParameter.class),
                        TypeIdentifier.from(CheckedException.class),
                        // メソッド内部
                        TypeIdentifier.from(InstructionField.class),
                        TypeIdentifier.from(UsedInstructionMethodReturn.class),
                        // TODO メソッドから戻ってくるだけの型は「使用している」から除外すべきかも
                        TypeIdentifier.from(UnusedInstructionMethodReturn.class),
                        TypeIdentifier.from(Instantiation.class),
                        TypeIdentifier.from(ReferenceConstantOwnerInMethod.class),
                        TypeIdentifier.from(ReferenceConstantInMethod.class),
                        TypeIdentifier.from(UseInLambda.class),
                        TypeIdentifier.from(MethodReference.class),
                        TypeIdentifier.from(UncheckedExceptionA.class),
                        TypeIdentifier.from(EnclosedClass.NestedClass.class),
                        TypeIdentifier.from(Integer.class),
                        TypeIdentifier.from(IntegerConstantFieldHolder.class),
                        TypeIdentifier.from(Consumer.class), // lambdaを受けるインタフェース or 呼び出しているメソッドの引数型
                        TypeIdentifier.from(int.class) // 参照しているフィールドの型
                )
                .doesNotContain(
                        // ローカル変数宣言だけで使用されている型は取得できない（コンパイルされたら消える）
                        TypeIdentifier.from(LocalValue.class),
                        // ネストされた型のエンクローズド型は名前空間を提供しているだけなので取得できない
                        TypeIdentifier.from(EnclosedClass.class),
                        // 呼び出し先のメソッドで宣言されているだけの例外
                        // throwsなどにも登場しなければ検出されない
                        TypeIdentifier.from(CheckedExceptionB.class),
                        TypeIdentifier.from(UncheckedExceptionB.class)
                );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_通常のメソッド呼び出し() throws Exception {
        var jigType = TestSupport.buildJigType(MethodInstructionTestStub.class);

        var list = jigType.instanceJigMethodStream()
                .filter(jigMethod -> jigMethod.declaration().asSignatureSimpleText().equals("method(MethodArgument)"))
                .toList();
        assertEquals(
                "[InstructionField.invokeMethod(), UsedInstructionMethodReturn.chainedInvokeMethod()]",
                list.get(0).usingMethods().methodDeclarations().asSimpleText()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_メソッド参照() throws Exception {
        var jigType = TestSupport.buildJigType(MethodInstructionTestStub.class);

        var method3 = jigType.instanceJigMethodStream()
                .filter(jigMethod -> jigMethod.declaration().asSignatureSimpleText().equals("methodRef()"))
                .toList();
        assertEquals(
                "[MethodReference.referenceMethod()]",
                method3.get(0).usingMethods().methodDeclarations().asSimpleText()
        );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる_lambda式() throws Exception {
        var jigType = TestSupport.buildJigType(MethodInstructionTestStub.class);

        var method2 = jigType.instanceJigMethodStream()
                .filter(jigMethod -> jigMethod.declaration().asSignatureSimpleText().equals("lambda()"))
                .toList();
        assertEquals(
                "[MethodInstructionTestStub.lambda$lambda$0(Object), Stream.empty(), Stream.forEach(Consumer)]",
                method2.get(0).usingMethods().methodDeclarations().asSimpleText()
        );

    }
}
