package jig.infrastructure.asm;

import jig.domain.model.specification.Specification;
import jig.domain.model.specification.SpecificationSource;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.kind.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AsmClassFileReaderTest {

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
                        Specification::hasMethod,
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
