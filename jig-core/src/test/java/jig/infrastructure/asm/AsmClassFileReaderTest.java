package jig.infrastructure.asm;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.specification.Specification;
import jig.domain.model.specification.SpecificationSource;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.kind.*;
import stub.domain.model.relation.RelationReadTarget;
import stub.domain.model.relation.MethodInstruction;
import stub.domain.model.relation.foo.Bar;
import stub.domain.model.relation.foo.Baz;
import stub.domain.model.relation.foo.Foo;
import stub.domain.model.relation.qux.Qux;
import stub.domain.model.relation.test.ArrayField;
import stub.domain.model.relation.test.FugaException;
import stub.domain.model.relation.test.LocalValue;
import stub.domain.model.relation.test.MethodArgument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AsmClassFileReaderTest {

    @Test
    void メソッド内で使用するクラスのテスト() throws Exception {
        Path path = Paths.get(MethodInstruction.class.getResource(MethodInstruction.class.getSimpleName().concat(".class")).toURI());

        AsmClassFileReader sut = new AsmClassFileReader();
        Specification actual = sut.readSpecification(new SpecificationSource(path));

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(Foo.class),
                        new TypeIdentifier(Bar.class),
                        new TypeIdentifier(Baz.class)
                )
                .doesNotContain(
                        new TypeIdentifier(LocalValue.class)
                );
    }

    @Test
    void relationReadTest() throws Exception {
        Path path = Paths.get(RelationReadTarget.class.getResource(RelationReadTarget.class.getSimpleName().concat(".class")).toURI());

        AsmClassFileReader sut = new AsmClassFileReader();
        Specification actual = sut.readSpecification(new SpecificationSource(path));

        TypeIdentifiers identifiers = actual.useTypes();

        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(Foo.class),
                        new TypeIdentifier(ArrayField.class.getName() + "[]"),
                        new TypeIdentifier(Bar.class),
                        new TypeIdentifier(Baz.class),
                        new TypeIdentifier(FugaException.class),
                        new TypeIdentifier(MethodArgument.class),
                        new TypeIdentifier(Qux.Quuz.class)
                        // new TypeIdentifier(UseInLambda.class)
                );
    }

    @ParameterizedTest
    @MethodSource
    void enumTest(Class<?> clz, boolean hasMethod, boolean hasField, boolean canExtend) throws Exception {
        Path path = Paths.get(clz.getResource(clz.getSimpleName().concat(".class")).toURI());
        SpecificationSources specificationSources = new SpecificationSources(Collections.singletonList(new SpecificationSource(path)));

        AsmClassFileReader sut = new AsmClassFileReader();
        Specifications actual = sut.readFrom(specificationSources);

        assertThat(actual.list()).hasSize(1)
                .first()
                .extracting(
                        Specification::isEnum,
                        Specification::hasInstanceMethod,
                        Specification::hasField,
                        Specification::canExtend
                )
                .containsExactly(
                        true,
                        hasMethod,
                        hasField,
                        canExtend
                );
    }

    static Stream<Arguments> enumTest() {
        return Stream.of(
                Arguments.of(SimpleEnum.class, false, false, false),
                Arguments.of(BehaviourEnum.class, true, false, false),
                Arguments.of(ParameterizedEnum.class, false, true, false),
                Arguments.of(PolymorphismEnum.class, false, false, true),
                Arguments.of(RichEnum.class, true, true, true));
    }
}
