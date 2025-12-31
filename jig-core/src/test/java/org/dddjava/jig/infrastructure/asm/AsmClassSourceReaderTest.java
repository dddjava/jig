package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.TypeKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.annotation.RuntimeRetainedAnnotation;
import stub.domain.model.category.RichEnum;
import stub.domain.model.category.SimpleEnum;
import stub.domain.model.relation.ClassDefinition;
import stub.domain.model.relation.EnumDefinition;
import stub.domain.model.relation.InterfaceDefinition;
import stub.domain.model.relation.clz.*;
import stub.domain.model.relation.enumeration.ClassReference;
import stub.domain.model.relation.enumeration.ConstructorParameter;
import stub.domain.model.relation.enumeration.EnumField;
import stub.domain.model.type.HogeRepository;
import stub.domain.model.type.SimpleNumber;
import testing.TestSupport;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AsmClassSourceReaderTest {

    @Test
    void クラス定義に使用している型が取得できる() {
        JigType actual = TestSupport.buildJigType(ClassDefinition.class);

        assertUsingTypesContainsAll(actual,
                ClassAnnotation.class, SuperClass.class,
                ImplementA.class, ImplementB.class,
                GenericsParameter.class);

        JigTypeReference superTypeData = actual.jigTypeHeader().baseTypeDataBundle().superType().orElseThrow();
        assertEquals("SuperClass<Integer, Long>", superTypeData.simpleNameWithGenerics());
        assertEquals(SuperClass.class.getName(), superTypeData.fqn());
    }

    @Test
    void インタフェース定義に使用している型が取得できる() {
        JigType actual = TestSupport.buildJigType(InterfaceDefinition.class);

        assertUsingTypesContainsAll(actual, ClassAnnotation.class, GenericsParameter.class);

        String actualText = actual.jigTypeHeader().baseTypeDataBundle().interfaceTypes().stream()
                .map(JigTypeReference::fqnWithGenerics)
                .collect(joining());
        assertEquals("java.lang.Comparable<stub.domain.model.relation.clz.GenericsParameter>", actualText);
    }

    @Test
    void enumで使用している型が取得できる() {
        JigType actual = TestSupport.buildJigType(EnumDefinition.class);

        assertUsingTypesContainsAll(actual, EnumField.class, ConstructorParameter.class, ClassReference.class);
    }

    private static void assertUsingTypesContainsAll(JigType jigType, Class<?>... classes) {
        var list = jigType.usingTypes().list();
        assertTrue(list.containsAll(Stream.of(classes)
                .map(TestSupport::getTypeIdFromClass)
                .toList()), "UsingTypes " + list + " Should contain all");
    }

    @MethodSource
    @ParameterizedTest
    void TypeKind判定(Class<?> targetType, TypeKind typeKind) {
        JigType actual = TestSupport.buildJigType(targetType);
        assertEquals(typeKind, actual.typeKind());
    }

    static Stream<Arguments> TypeKind判定() {
        return Stream.of(
                arguments(SimpleNumber.class, TypeKind.通常型),
                arguments(SimpleEnum.class, TypeKind.列挙型),
                arguments(RichEnum.class, TypeKind.抽象列挙型),
                // インタフェースと判定させたい
                arguments(HogeRepository.class, TypeKind.通常型),
                // アノテーションと判定させたい
                arguments(RuntimeRetainedAnnotation.class, TypeKind.通常型)
        );
    }
}
