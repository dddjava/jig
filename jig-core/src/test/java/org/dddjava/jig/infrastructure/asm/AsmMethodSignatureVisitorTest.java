package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AsmMethodSignatureVisitorTest {
    private static final Logger logger = LoggerFactory.getLogger(AsmMethodSignatureVisitorTest.class);

    @MethodSource
    @ParameterizedTest
    void test(String methodName, String expectedSignature, String expectedJigMethodSignatureSimpleText) throws IOException {
        var map = new HashMap<String, String>();
        // メソッドのシグネチャを取得する
        new ClassReader(JigSupportMethodSignatureImplementations.class.getName())
                .accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        map.put(name, signature);
                        return null;
                    }
                }, 0);

        // シグネチャの検証
        var actualSignature = map.get(methodName);
        assertEquals(expectedSignature, actualSignature);

        AsmMethodSignatureVisitor sut = new AsmMethodSignatureVisitor(Opcodes.ASM9);
        logger.debug("read signature:{}", actualSignature);
        new SignatureReader(actualSignature).accept(sut);

        MethodDeclaration methodDeclaration = sut.buildMethodDeclaration(TypeIdentifier.from(JigSupportMethodSignatureImplementations.class), methodName);

        assertEquals(expectedJigMethodSignatureSimpleText, methodDeclaration.asSignatureAndReturnTypeSimpleText());
    }

    static Stream<Arguments> test() throws IOException {
        return Stream.of(
                arguments("returnsGenericMethod",
                        "<T:Ljava/lang/Object;>()TT;",
                        "returnsGenericMethod():T"),
                arguments("returnsGenericMethodWithArguments",
                        "<T1:Ljava/lang/Object;T2:Ljava/lang/Object;>(TT1;TT2;)TT2;",
                        "returnsGenericMethodWithArguments(T1, T2):T2"),
                arguments("genericArgumentMethod",
                        "(Ljava/util/List<Ljava/lang/String;>;)V",
                        "genericArgumentMethod(List<String>):void"),
                arguments("bindMethod",
                        "(Ljava/util/Optional<Ljava/lang/Long;>;)Ljava/util/Optional<Ljava/lang/Integer;>;",
                        "bindMethod(Optional<Long>):Optional<Integer>"),
                arguments("nestedBindMethod",
                        "(Ljava/util/Optional<Ljava/util/Optional<Ljava/lang/Long;>;>;)Ljava/util/Optional<Ljava/util/Optional<Ljava/lang/Integer;>;>;",
                        "nestedBindMethod(Optional<Optional<Long>>):Optional<Optional<Integer>>")
        );
    }

    static class JigSupportMethodSignatureImplementations {

        void returnsVoidMethod() {
        }

        int returnsPrimitiveMethod() {
            return 0;
        }

        String returnsStringMethod() {
            return "";
        }

        <T> T returnsGenericMethod() {
            return null;
        }

        <T1, T2> T2 returnsGenericMethodWithArguments(T1 t1, T2 t2) {
            return null;
        }

        void stringArgumentMethod(String string) {
        }

        void primitiveArgumentMethod(int i) {
        }

        void genericArgumentMethod(List<String> list) {
        }

        void varargsMethod(LocalDate... localDates) {
        }

        Optional<Integer> bindMethod(Optional<Long> optional) {
            return Optional.empty();
        }

        Optional<Optional<Integer>> nestedBindMethod(Optional<Optional<Long>> optional) {
            return Optional.empty();
        }
    }
}
