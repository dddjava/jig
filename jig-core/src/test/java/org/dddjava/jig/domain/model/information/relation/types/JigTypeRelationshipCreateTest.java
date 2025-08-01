package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JigTypeRelationshipCreateTest {

    private static class SimpleClass {
    }

    private static class ComplexSubClass<T> extends SimpleClass {
    }

    private interface ComplexInterface<T> {
    }

    private static class ComplexClass<T1 extends CharSequence> extends ComplexSubClass<String> implements ComplexInterface<Integer> {
    }

    @Test
    void simpleClassTest() {
        JigType jigType = TestSupport.buildJigType(SimpleClass.class);
        TypeRelationships sut = TypeRelationships.from(jigType);

        assertEquals(1, sut.size());
        assertEquals(
                TypeRelationship.of(
                        TypeId.from(SimpleClass.class), TypeId.from(Object.class),
                        TypeRelationKind.継承クラス),
                sut.typeRelationships().stream().findFirst().orElseThrow());
    }

    @Test
    void complexClassTest() {
        JigType jigType = TestSupport.buildJigType(ComplexClass.class);
        TypeRelationships sut = TypeRelationships.from(jigType);

        assertEquals(5, sut.size());
        Comparator<TypeRelationship> comparing = Comparator.comparing(TypeRelationship::to);
        TypeId from = TypeId.from(ComplexClass.class);
        assertEquals(Stream.of(
                        TypeRelationship.of(from, TypeId.from(CharSequence.class), TypeRelationKind.型引数),
                        TypeRelationship.of(from, TypeId.from(ComplexSubClass.class), TypeRelationKind.継承クラス),
                        TypeRelationship.of(from, TypeId.from(String.class), TypeRelationKind.型引数),
                        TypeRelationship.of(from, TypeId.from(ComplexInterface.class), TypeRelationKind.実装インタフェース),
                        TypeRelationship.of(from, TypeId.from(Integer.class), TypeRelationKind.型引数)
                ).sorted(comparing).toList(),
                sut.list().stream().sorted(comparing).toList());
    }
}
