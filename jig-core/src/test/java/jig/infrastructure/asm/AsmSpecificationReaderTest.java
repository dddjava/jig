package jig.infrastructure.asm;

import jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.specification.MethodSpecification;
import jig.domain.model.specification.Specification;
import jig.domain.model.specification.SpecificationSource;
import jig.infrastructure.PropertySpecificationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.Annotated;
import stub.domain.model.kind.*;
import stub.domain.model.relation.ClassDefinition;
import stub.domain.model.relation.EnumDefinition;
import stub.domain.model.relation.FieldDefinition;
import stub.domain.model.relation.MethodInstruction;
import stub.domain.model.relation.foo.Bar;
import stub.domain.model.relation.foo.Baz;
import stub.domain.model.relation.foo.Foo;
import stub.domain.model.relation.test.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AsmSpecificationReaderTest {

    @Test
    void アノテーションの読み取り() throws Exception {
        Specification actual = exercise(Annotated.class);

        List<FieldAnnotationDeclaration> fieldAnnotationDeclarations = actual.fieldAnnotationDeclarations();
        assertThat(fieldAnnotationDeclarations)
                .hasSize(1)
                .first()
                .satisfies(fieldAnnotationDeclaration -> {
                    assertThat(fieldAnnotationDeclaration.annotationType().fullQualifiedName()).isEqualTo(VariableAnnotation.class.getTypeName());
                    assertThat(fieldAnnotationDeclaration.fieldDeclaration().nameText()).isEqualTo("field");

                    String descriptionText = fieldAnnotationDeclaration.description().asText();
                    assertThat(descriptionText).isNotEqualTo("[]");
                });

        List<MethodSpecification> methodSpecifications = actual.instanceMethodSpecifications();
        assertThat(methodSpecifications)
                .hasSize(1)
                .first()
                .satisfies(methodSpecification -> {
                    List<MethodAnnotationDeclaration> methodAnnotationDeclarations = methodSpecification.methodAnnotationDeclarations();
                    assertThat(methodAnnotationDeclarations)
                            .hasSize(1)
                            .first()
                            .satisfies(methodAnnotationDeclaration -> {
                                assertThat(methodAnnotationDeclaration.annotationType().fullQualifiedName()).isEqualTo(VariableAnnotation.class.getTypeName());
                                assertThat(methodAnnotationDeclaration.methodDeclaration().asSimpleText()).isEqualTo("method()");

                                String descriptionText = methodAnnotationDeclaration.description().asText();
                                assertThat(descriptionText).isNotEqualTo("[]");
                            });
                });
    }

    @Test
    void クラス定義のテスト() throws Exception {
        Specification actual = exercise(ClassDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(RetentionClassAnnotation.class),
                        new TypeIdentifier(SuperClass.class),
                        new TypeIdentifier(ImplementA.class),
                        new TypeIdentifier(ImplementB.class),
                        new TypeIdentifier(GenericArgument.class)
                );
    }

    @Test
    void フィールド定義のテスト() throws Exception {
        Specification actual = exercise(FieldDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(RetentionClassAnnotation.class),
                        new TypeIdentifier(StaticField.class),
                        new TypeIdentifier(InstanceField.class),
                        new TypeIdentifier(GenericField.class),
                        new TypeIdentifier(ArrayField.class.getName() + "[]"),
                        new TypeIdentifier(ArrayField.class),
                        new TypeIdentifier(FieldReference.class),
                        new TypeIdentifier(ReferenceField.class)
                );
    }

    @Test
    void メソッドで使用するクラスのテスト() throws Exception {
        Specification actual = exercise(MethodInstruction.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(RetentionClassAnnotation.class),
                        new TypeIdentifier(Foo.class),
                        new TypeIdentifier(Bar.class),
                        new TypeIdentifier(Baz.class),
                        new TypeIdentifier(Instantiation.class),
                        new TypeIdentifier(FugaException.class),
                        new TypeIdentifier(MethodArgument.class),
                        new TypeIdentifier(GenericArgument.class),
                        new TypeIdentifier(FieldReference.class),
                        new TypeIdentifier(ReferenceField.class),
                        new TypeIdentifier(UseInLambda.class),
                        new TypeIdentifier(MethodReference.class),
                        new TypeIdentifier(EnclosedClass.NestedClass.class),
                        new TypeIdentifier(ThrowingException.class)
                )
                .doesNotContain(
                        new TypeIdentifier(LocalValue.class),
                        new TypeIdentifier(EnclosedClass.class)
                );

        assertThat(actual.hasField()).isFalse();
    }

    @Test
    void enumで使用するクラスのテスト() throws Exception {
        Specification actual = exercise(EnumDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(InstanceField.class),
                        new TypeIdentifier(ConstructorArgument.class),
                        new TypeIdentifier(ClassReference.class)
                );
    }

    @ParameterizedTest
    @MethodSource
    void enumTest(Class<?> clz, boolean hasMethod, boolean hasField, boolean canExtend) throws Exception {
        Specification actual = exercise(clz);

        assertThat(actual)
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

    private Specification exercise(Class<?> definitionClass) throws URISyntaxException {
        Path path = Paths.get(definitionClass.getResource(definitionClass.getSimpleName().concat(".class")).toURI());

        AsmSpecificationReader sut = new AsmSpecificationReader(new PropertySpecificationContext());
        return sut.readSpecification(new SpecificationSource(path));
    }
}
