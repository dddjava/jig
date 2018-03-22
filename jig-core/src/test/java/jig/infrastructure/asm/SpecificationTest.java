package jig.infrastructure.asm;

import jig.domain.model.specification.Specification;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import stub.type.kind.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecificationTest {

    @ParameterizedTest
    @MethodSource
    void enumTest(Class<?> clz, boolean hasMethod, boolean hasField, boolean canExtend) throws IOException {
        String name = clz.getTypeName().replace('.', '/') + ".class";
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(name)) {

            SpecificationReadingVisitor visitor = new SpecificationReadingVisitor();
            System.out.println(is);
            ClassReader classReader = new ClassReader(is);
            classReader.accept(visitor, ClassReader.SKIP_DEBUG);

            Specification specification = visitor.specification();

            assertThat(specification.isEnum()).isTrue();
            assertThat(specification.hasMethod()).isEqualTo(hasMethod);
            assertThat(specification.hasField()).isEqualTo(hasField);
            assertThat(specification.canExtend()).isEqualTo(canExtend);
        }
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
