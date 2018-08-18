package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSource;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import stub.domain.model.relation.ConstructorInstruction;
import stub.domain.model.relation.MethodInstruction;
import stub.domain.model.relation.StaticMethodInstruction;
import stub.domain.model.relation.constant.to_primitive_wrapper_constant.IntegerConstantFieldHolder;
import stub.domain.model.relation.method.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class MethodInstructionTest {

    @ParameterizedTest
    @ValueSource(classes = {MethodInstruction.class, StaticMethodInstruction.class, ConstructorInstruction.class})
    void メソッドで使用している型が取得できる(Class<?> clz) throws Exception {
        TypeByteCode actual = exercise(clz);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .containsExactlyInAnyOrder(
                        // 標準
                        new TypeIdentifier(Object.class),
                        new TypeIdentifier(String.class),
                        new TypeIdentifier(List.class),
                        new TypeIdentifier(Stream.class),
                        new TypeIdentifier("void"),
                        new TypeIdentifier(Exception.class),
                        // 自身への参照（コンストラクタ？）
                        new TypeIdentifier(clz),
                        // メソッド定義
                        new TypeIdentifier(MethodAnnotation.class),
                        new TypeIdentifier(MethodArgument.class),
                        new TypeIdentifier(MethodReturn.class),
                        new TypeIdentifier(ArgumentGenericsParameter.class),
                        new TypeIdentifier(CheckedException.class),
                        // メソッド内部
                        new TypeIdentifier(InstructionField.class),
                        new TypeIdentifier(UsedInstructionMethodReturn.class),
                        // TODO メソッドから戻ってくるだけの型は「使用している」から除外すべきかも
                        new TypeIdentifier(UnusedInstructionMethodReturn.class),
                        new TypeIdentifier(Instantiation.class),
                        new TypeIdentifier(ReferenceConstantOwnerInMethod.class),
                        new TypeIdentifier(ReferenceConstantInMethod.class),
                        new TypeIdentifier(UseInLambda.class),
                        new TypeIdentifier(MethodReference.class),
                        new TypeIdentifier(UncheckedExceptionA.class),
                        new TypeIdentifier(EnclosedClass.NestedClass.class),
                        new TypeIdentifier(Integer.class),
                        new TypeIdentifier(IntegerConstantFieldHolder.class)
                )
                .doesNotContain(
                        // ローカル変数宣言だけで使用されている型は取得できない（コンパイルされたら消える）
                        new TypeIdentifier(LocalValue.class),
                        // ネストされた型のエンクローズド型は名前空間を提供しているだけなので取得できない
                        new TypeIdentifier(EnclosedClass.class),
                        // 呼び出し先のメソッドで宣言されているだけの例外
                        // throwsなどにも登場しなければ検出されない
                        new TypeIdentifier(CheckedExceptionB.class),
                        new TypeIdentifier(UncheckedExceptionB.class)
                );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる() throws Exception {
        TypeByteCode actual = exercise(MethodInstruction.class);

        assertThat(actual.instanceMethodByteCodes())
                .extracting(
                        methodByteCode -> methodByteCode.methodDeclaration.asSignatureSimpleText(),
                        methodByteCode -> methodByteCode.usingMethods().asSimpleText()
                )
                .contains(
                        tuple("method(MethodArgument)", "[InstructionField.invokeMethod(), UsedInstructionMethodReturn.chainedInvokeMethod()]"),
                        tuple("lambda()", "[MethodInstruction.lambda$lambda$0(Object), Stream.empty(), Stream.forEach(Consumer)]")
                );
    }

    private TypeByteCode exercise(Class<?> definitionClass) throws URISyntaxException {
        Path path = Paths.get(definitionClass.getResource(definitionClass.getSimpleName().concat(".class")).toURI());

        AsmByteCodeFactory sut = new AsmByteCodeFactory();
        return sut.analyze(new ByteCodeSource(path));
    }
}
