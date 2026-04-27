package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JigTypeRelationshipCreateTest {

    private static class SimpleClass {
    }

    private static class ClassWithGenericField {
        java.util.List<String> items;
    }

    private static class ClassWithGenericMethod {
        java.util.Optional<String> get() { return java.util.Optional.empty(); }
        void add(java.util.List<String> items) {}
    }

    private static class ComplexSubClass<T> extends SimpleClass {
    }

    private interface ComplexInterface<T> {
    }

    private static class ComplexClass<T1 extends CharSequence> extends ComplexSubClass<String> implements ComplexInterface<Integer> {
    }

    @Test
    void ジェネリクスのフィールド型と型引数が取得できる() {
        JigType jigType = TestSupport.buildJigType(ClassWithGenericField.class);
        TypeRelationships sut = TypeRelationships.from(jigType);

        TypeId from = TestSupport.getTypeIdFromClass(ClassWithGenericField.class);
        assertTrue(sut.relationships().contains(
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(java.util.List.class), TypeRelationKind.フィールド型)),
                "List はフィールド型であること");
        assertTrue(sut.relationships().contains(
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(String.class), TypeRelationKind.フィールド型引数)),
                "String はフィールド型引数であること");
    }

    @Test
    void ジェネリクスのメソッド戻り値と引数の型が取得できる() {
        JigType jigType = TestSupport.buildJigType(ClassWithGenericMethod.class);
        TypeRelationships sut = TypeRelationships.from(jigType);

        TypeId from = TestSupport.getTypeIdFromClass(ClassWithGenericMethod.class);
        assertTrue(sut.relationships().contains(
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(java.util.Optional.class), TypeRelationKind.メソッド戻り値)),
                "Optional はメソッド戻り値であること");
        assertTrue(sut.relationships().contains(
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(String.class), TypeRelationKind.メソッド戻り値型引数)),
                "String はメソッド戻り値型引数であること");
        assertTrue(sut.relationships().contains(
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(java.util.List.class), TypeRelationKind.メソッド引数)),
                "List はメソッド引数であること");
        // String は メソッド戻り値型引数 と メソッド引数型引数 の両方で現れるが、from-to-kind のトリプルで重複排除されない
        assertTrue(sut.relationships().stream().anyMatch(rel ->
                        rel.equals(TypeRelationship.of(from, TestSupport.getTypeIdFromClass(String.class), TypeRelationKind.メソッド引数型引数))),
                "List<String> の String はメソッド引数型引数であること");
    }

    @Test
    void 単純なクラスで継承関係が取得できる() {
        JigType jigType = TestSupport.buildJigType(SimpleClass.class);
        TypeRelationships sut = TypeRelationships.from(jigType);

        TypeId from = TestSupport.getTypeIdFromClass(SimpleClass.class);
        assertTrue(sut.relationships().contains(
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(Object.class), TypeRelationKind.継承クラス)));
    }

    @Test
    void 複数の型パラメータや継承を持つクラスで全関係が取得できる() {
        JigType jigType = TestSupport.buildJigType(ComplexClass.class);
        TypeRelationships sut = TypeRelationships.from(jigType);

        TypeId from = TestSupport.getTypeIdFromClass(ComplexClass.class);
        var expected = Stream.of(
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(CharSequence.class), TypeRelationKind.型引数),
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(ComplexSubClass.class), TypeRelationKind.継承クラス),
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(String.class), TypeRelationKind.型引数),
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(ComplexInterface.class), TypeRelationKind.実装インタフェース),
                TypeRelationship.of(from, TestSupport.getTypeIdFromClass(Integer.class), TypeRelationKind.型引数)
        ).toList();
        assertTrue(sut.relationships().containsAll(expected),
                "Expected relations not found. Actual: " + sut.relationships());
    }
}
