package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
class MethodSignatureVisitorTest {

    @MethodSource
    @ParameterizedTest
    void test(String signature, String expected) {
        MethodSignatureVisitor sut = new MethodSignatureVisitor(Opcodes.ASM9);
        new SignatureReader(signature).accept(sut);

        MethodDeclaration methodDeclaration = sut.methodDeclaration(new TypeIdentifier("Dummy"), "method");

        assertEquals(expected, methodDeclaration.asSignatureAndReturnTypeSimpleText());
    }

    static Stream<Arguments> test() {
        return Stream.of(
                Arguments.of("()V", "method():void"),
                Arguments.of("()I", "method():int"),
                Arguments.of("([J)[I", "method(int[]):long[]"),
                Arguments.of("(TT1;)TT2;", "method(T1):T2"), // 型パラメタ
                Arguments.of("()Lhoge/Hoge;", "method():Hoge"),
                // 引数のジェネリクス対応したら method(List<String):List<String> になる予定
                Arguments.of("(Ljava/util/List<Ljava/lang/String;>;)Ljava/util/List<Ljava/lang/String;>;", "method(List):List<String>")
        );
    }
}