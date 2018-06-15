package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSource;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;
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
import stub.misc.DecisionClass;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class AsmByteCodeReaderTest {

    @Test
    void 付与されているアノテーションと記述が取得できる() throws Exception {
        ByteCode actual = exercise(Annotated.class);

        assertThat(actual.fieldAnnotationDeclarations())
                .hasSize(1)
                .first()
                .satisfies(fieldAnnotationDeclaration -> {
                    assertThat(fieldAnnotationDeclaration.annotationType().fullQualifiedName()).isEqualTo(VariableAnnotation.class.getTypeName());
                    assertThat(fieldAnnotationDeclaration.fieldDeclaration().nameText()).isEqualTo("field");

                    String descriptionText = fieldAnnotationDeclaration.description().asText();
                    assertThat(descriptionText).isEqualTo("[string = \"af\", arrayString = [...], number = 13, clz = Ljava/lang/reflect/Field;, arrayClz = [...], enumValue = DUMMY1, annotation = Ljava/lang/Deprecated;[...]]");
                });

        assertThat(actual.instanceMethodByteCodes())
                .hasSize(1)
                .first()
                .satisfies(methodByteCode -> {
                    assertThat(methodByteCode.methodAnnotationDeclarations())
                            .hasSize(1)
                            .first()
                            .satisfies(methodAnnotationDeclaration -> {
                                assertThat(methodAnnotationDeclaration.annotationType().fullQualifiedName()).isEqualTo(VariableAnnotation.class.getTypeName());
                                assertThat(methodAnnotationDeclaration.methodDeclaration().asSignatureSimpleText()).isEqualTo("method()");

                                String descriptionText = methodAnnotationDeclaration.description().asText();
                                assertThat(descriptionText).isEqualTo("[string = \"am\", arrayString = [...], number = 23, clz = Ljava/lang/reflect/Method;, enumValue = DUMMY2]");
                            });
                });
    }

    @Test
    void クラス定義に使用している型が取得できる() throws Exception {
        ByteCode actual = exercise(ClassDefinition.class);

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
    void フィールド定義に使用している型が取得できる() throws Exception {
        ByteCode actual = exercise(FieldDefinition.class);

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
    void メソッドで使用している型が取得できる() throws Exception {
        ByteCode actual = exercise(MethodInstruction.class);

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
                        // ローカル変数宣言だけで使用されている型は取得できない（コンパイルされたら消える）
                        new TypeIdentifier(LocalValue.class),
                        // ネストされた型のエンクローズド型は名前空間を提供しているだけなので取得できない
                        new TypeIdentifier(EnclosedClass.class)
                );
    }

    @Test
    void メソッドの使用しているメソッドが取得できる() throws Exception {
        ByteCode actual = exercise(MethodInstruction.class);

        assertThat(actual.instanceMethodByteCodes())
                .extracting(
                        methodByteCode -> methodByteCode.methodDeclaration.asSignatureSimpleText(),
                        methodByteCode -> methodByteCode.usingMethods().asSimpleText()
                )
                .filteredOn(tuple -> {
                    Object methodDeclaration = tuple.toArray()[0];
                    return methodDeclaration.equals("method(MethodArgument)") || methodDeclaration.equals("lambda()");
                })
                .containsExactlyInAnyOrder(
                        tuple("method(MethodArgument)", "[Bar.toBaz(), Foo.toBar()]"),
                        tuple("lambda()", "[MethodInstruction.lambda$lambda$0(Object), Stream.empty(), Stream.forEach(Consumer)]")
                );
    }

    @Test
    void メソッドでifやswitchを使用していると検出できる() throws Exception {
        ByteCode actual = exercise(DecisionClass.class);

        List<MethodByteCode> methodByteCodes = actual.instanceMethodByteCodes();

        assertThat(methodByteCodes)
                .extracting(
                        methodByteCode -> methodByteCode.methodDeclaration.asSignatureSimpleText(),
                        MethodByteCode::hasDecision)
                .containsExactlyInAnyOrder(
                        tuple("分岐なしメソッド()", false),
                        tuple("ifがあるメソッド()", true),
                        tuple("switchがあるメソッド()", true),
                        // forは ifeq と goto で構成されるのでifと区別つかない
                        tuple("forがあるメソッド()", true)
                );
    }

    @Test
    void enumで使用している型が取得できる() throws Exception {
        ByteCode actual = exercise(EnumDefinition.class);

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
    void enumの特徴づけに必要な情報が取得できる(Class<?> clz, boolean hasMethod, boolean hasField, boolean canExtend) throws Exception {
        ByteCode actual = exercise(clz);

        assertThat(actual)
                .extracting(
                        ByteCode::isEnum,
                        ByteCode::hasInstanceMethod,
                        ByteCode::hasField,
                        ByteCode::canExtend
                )
                .containsExactly(
                        true,
                        hasMethod,
                        hasField,
                        canExtend
                );
    }

    static Stream<Arguments> enumの特徴づけに必要な情報が取得できる() {
        return Stream.of(
                Arguments.of(SimpleEnum.class, false, false, false),
                Arguments.of(BehaviourEnum.class, true, false, false),
                Arguments.of(ParameterizedEnum.class, false, true, false),
                Arguments.of(PolymorphismEnum.class, false, false, true),
                Arguments.of(RichEnum.class, true, true, true));
    }

    private ByteCode exercise(Class<?> definitionClass) throws URISyntaxException {
        Path path = Paths.get(definitionClass.getResource(definitionClass.getSimpleName().concat(".class")).toURI());

        AsmByteCodeFactory sut = new AsmByteCodeFactory();
        return sut.analyze(new ByteCodeSource(path));
    }
}
