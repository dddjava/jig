package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.method.JigMethod;
import org.dddjava.jig.domain.model.information.type.JigTypeMembers;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import stub.domain.model.relation.annotation.UseInAnnotation;
import stub.domain.model.relation.annotation.VariableAnnotation;
import stub.misc.DecisionClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MethodVisitorはClassVisitor経由でテストする
 */
class AsmMethodVisitorTest {

    private static class MethodVisitorSut {

        @SutAnnotations.A1
        Object メソッドで使用している基本的な型が取得できる(boolean b1, @SutAnnotations.B Boolean b2) throws @SutAnnotations.C NoSuchElementException {
            @SutAnnotations.D1 @SutAnnotations.D2 String str = String.valueOf('a');
            int exact = Math.toIntExact(BigDecimal.ZERO.longValueExact());
            return Void.class;
        }

        Optional<Predicate<Function<Integer, Character>>> メソッドで使用しているジェネリクスが取得できる(Supplier<UnaryOperator<Consumer<Long>>> parameter) {
            return Optional.empty();
        }

        void 引数型のジェネリクスが取得できる(List<String> list) {
        }

        List<String> 戻り値のジェネリクスが取得できる() {
            return null;
        }

        @VariableAnnotation(string = "am", arrayString = {"bm1", "bm2"}, number = 23, clz = Method.class, enumValue = UseInAnnotation.DUMMY2)
        void メソッドに付与されているアノテーションと記述が取得できる() {
        }
    }

    static class SutAnnotations {
        @Target({ElementType.METHOD, ElementType.TYPE_USE})
        @interface A1 {
        }

        @Target(ElementType.TYPE_USE)
        @interface A2 {
        }

        @Target(ElementType.TYPE_USE)
        @interface B {
        }

        @Target(ElementType.TYPE_USE)
        @interface C {
        }

        @Target(ElementType.LOCAL_VARIABLE)
        @interface D1 {
        }

        @Target(ElementType.TYPE_USE)
        @interface D2 {
        }
    }

    private static class MethodReturnAndArgumentsSut {

        void returnsVoidMethod() {
        }

        int primitiveMethod(float f) {
            return 0;
        }

        String normalMethod(BigDecimal bigDecimal) {
            return "";
        }

        <T> T genericMethod1() {
            return null;
        }

        <T1, T2> T2 genericMethod2(T1 t1, T2 t2) {
            return null;
        }

        Optional<Integer> genericArgumentMethod1(List<String> list) {
            return Optional.empty();
        }

        <T, U, V> T genericArgumentMethod2(Map<U, String> map, V v) {
            return null;
        }

        Optional<Optional<Integer>> genericArgumentMethod3(Optional<Optional<Long>> optional) {
            return Optional.empty();
        }

        void varargsMethod(LocalDate... localDates) {
        }
    }

    @Test
    void メソッドで使用している型が取得できる() {
        JigMethod method = JigMethod準備(MethodVisitorSut.class, "メソッドで使用している基本的な型が取得できる");

        Set<String> actual = method.usingTypes().list()
                // アサーションのための名前でsetで収集する
                .stream().map(TypeIdentifier::asSimpleName).collect(Collectors.toSet());

        // "A2", "B", "C" などのTypeAnnotationは取得できていない
        // メソッド内のアノテーションは取得できていない
        Set<String> expected = Set.of(
                "A1", // メソッドアノテーション
                "Object", // 戻り値
                "boolean", "Boolean", // 引数
                "NoSuchElementException", // throws
                "String", "char", // 1行目
                "Math", "BigDecimal", "long", "int", // 2行目
                "Void" // return
        );
        assertEquals(expected, actual);
    }

    @Test
    void メソッドで使用しているジェネリクスが取得できる() {
        JigMethod method = JigMethod準備(MethodVisitorSut.class, "メソッドで使用しているジェネリクスが取得できる");

        var actual = method.usingTypes().list()
                // アサーションのための名前でsetで収集する
                .stream().map(TypeIdentifier::asSimpleName).collect(Collectors.toSet());

        var expected = Set.of(
                "Optional", "Predicate", "Function", "Integer", "Character", // 戻り値
                "Supplier", "UnaryOperator", "Consumer", "Long" // 引数
        );
        assertEquals(expected, actual);
    }

    @Test
    void メソッドに付与されているアノテーションと記述が取得できる() throws Exception {
        JigMethod method = JigMethod準備(MethodVisitorSut.class, "メソッドに付与されているアノテーションと記述が取得できる");
        MethodAnnotation methodAnnotation = method.methodAnnotations().list().get(0);

        assertThat(methodAnnotation.annotationType().fullQualifiedName()).isEqualTo(VariableAnnotation.class.getTypeName());

        AnnotationDescription description = methodAnnotation.description();
        assertThat(description.asText())
                .contains(
                        "string=am",
                        "arrayString={bm1, bm2}",
                        "number=23",
                        "clz=Method",
                        "enumValue=UseInAnnotation.DUMMY2"
                );
    }

    @Test
    void 戻り値のジェネリクスが取得できる() throws Exception {
        JigMethod actual = JigMethod準備(MethodVisitorSut.class, "戻り値のジェネリクスが取得できる");

        assertEquals("List<String>", actual.declaration().methodReturn().parameterizedType().asSimpleText());
    }

    @Test
    void 引数型のジェネリクスが取得できる() {
        JigMethod actual = JigMethod準備(MethodVisitorSut.class, "引数型のジェネリクスが取得できる");

        assertEquals("引数型のジェネリクスが取得できる(java.util.List<java.lang.String>)", actual.declaration().methodSignature().asText());
    }

    @CsvSource({
            "分岐なしメソッド, 0",
            "ifがあるメソッド, 1",
            "switchがあるメソッド, 1",
            // forは ifeq と goto で構成されるある意味での分岐
            "forがあるメソッド, 1",
    })
    @ParameterizedTest
    void メソッドでifやswitchを使用していると検出できる(String name, int number) throws Exception {
        JigMethod actual = JigMethod準備(DecisionClass.class, name);
        assertEquals(number, actual.decisionNumber().intValue());
    }

    @CsvSource({
            "returnsVoidMethod,       returnsVoidMethod():void",
            "primitiveMethod,         primitiveMethod(float):int",
            "normalMethod,            normalMethod(BigDecimal):String",
            "genericMethod1,          genericMethod1():T",
            "genericMethod2,          'genericMethod2(T1, T2):T2'",
            "genericArgumentMethod1,  genericArgumentMethod1(List<String>):Optional<Integer>",
            "genericArgumentMethod2,  'genericArgumentMethod2(Map<U, String>, V):T'",
            "genericArgumentMethod3,  genericArgumentMethod3(Optional<Optional<Long>>):Optional<Optional<Integer>>",
            "varargsMethod,           varargsMethod(LocalDate[]):void",
    })
    @ParameterizedTest
    void 引数と戻り値を文字列表示できる(String methodName, String expectedText) {
        JigMethod actual = JigMethod準備(MethodReturnAndArgumentsSut.class, methodName);
        assertEquals(expectedText, actual.asNameArgumentsReturnText());
    }

    private static JigMethod JigMethod準備(Class<?> sutClass, String methodName) {
        JigTypeMembers members = 準備(sutClass).buildJigTypeMembers();
        return members.jigMethodStream()
                .filter(jigMethod -> jigMethod.name().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private static JigMethodDeclaration JigMethodDeclaration準備(Class<?> sutClass, String methodName) {
        var members = 準備(sutClass).buildJigTypeMembers();
        Collection<JigMethodDeclaration> methodByName = members.findMethodByName(methodName);
        return methodByName.stream().findFirst().orElseThrow();
    }

    private static JigMemberBuilder 準備(Class<?> sutClass) {
        return AsmClassVisitorTest.asmClassVisitor(sutClass).jigMemberBuilder();
    }
}