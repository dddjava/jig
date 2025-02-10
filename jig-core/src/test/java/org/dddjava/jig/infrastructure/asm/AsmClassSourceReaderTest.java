package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
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
import stub.domain.model.relation.enumeration.ConstructorArgument;
import stub.domain.model.relation.enumeration.EnumField;
import stub.domain.model.type.HogeRepository;
import stub.domain.model.type.SimpleNumber;
import testing.TestSupport;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AsmClassSourceReaderTest {

    @Test
    void クラス定義に使用している型が取得できる() throws Exception {
        JigType actual = TestSupport.buildJigType(ClassDefinition.class);

        TypeIdentifiers identifiers = actual.usingTypes();
        assertThat(identifiers.list())
                .contains(
                        TypeIdentifier.from(ClassAnnotation.class),
                        TypeIdentifier.from(SuperClass.class),
                        TypeIdentifier.from(ImplementA.class),
                        TypeIdentifier.from(ImplementB.class),
                        TypeIdentifier.from(GenericsParameter.class)
                );

        JigTypeReference superTypeData = actual.jigTypeHeader().baseTypeDataBundle().superType().orElseThrow();
        assertEquals("SuperClass<Integer, Long>", superTypeData.simpleNameWithGenerics());
        assertEquals(SuperClass.class.getName(), superTypeData.fqn());
    }

    @Test
    void インタフェース定義に使用している型が取得できる() throws Exception {
        JigType actual = TestSupport.buildJigType(InterfaceDefinition.class);

        TypeIdentifiers identifiers = actual.usingTypes();
        assertThat(identifiers.list())
                .contains(
                        TypeIdentifier.from(ClassAnnotation.class),
                        TypeIdentifier.from(Comparable.class),
                        TypeIdentifier.from(GenericsParameter.class)
                );

        String actualText = actual.jigTypeHeader().baseTypeDataBundle().interfaceTypes().stream()
                .map(JigTypeReference::fqnWithGenerics)
                .collect(Collectors.joining());
        assertEquals("java.lang.Comparable<stub.domain.model.relation.clz.GenericsParameter>", actualText);
    }

    @Test
    void enumで使用している型が取得できる() throws Exception {
        JigType actual = TestSupport.buildJigType(EnumDefinition.class);

        TypeIdentifiers identifiers = actual.usingTypes();
        assertThat(identifiers.list())
                .contains(
                        TypeIdentifier.from(EnumField.class),
                        TypeIdentifier.from(ConstructorArgument.class),
                        TypeIdentifier.from(ClassReference.class)
                );
    }

    @MethodSource
    @ParameterizedTest
    void TypeKind判定(Class<?> targetType, TypeKind typeKind) throws Exception {
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
