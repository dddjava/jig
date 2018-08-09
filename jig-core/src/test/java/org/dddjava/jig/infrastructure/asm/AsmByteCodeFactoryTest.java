package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.declaration.type.ParameterizedType;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
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
import stub.domain.model.relation.annotation.VariableAnnotation;
import stub.domain.model.relation.clz.*;
import stub.domain.model.relation.enumeration.ClassReference;
import stub.domain.model.relation.enumeration.ConstructorArgument;
import stub.domain.model.relation.enumeration.EnumField;
import stub.domain.model.relation.field.*;
import stub.misc.DecisionClass;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class AsmByteCodeFactoryTest {

    @Test
    void フィールドに付与されているアノテーションと記述が取得できる() throws Exception {
        TypeByteCode actual = exercise(Annotated.class);

        List<FieldAnnotation> fieldAnnotations = actual.annotatedFields();
        FieldAnnotation fieldAnnotation = fieldAnnotations.stream()
                .filter(e -> e.fieldDeclaration().nameText().equals("field"))
                .findFirst().orElseThrow(AssertionError::new);

        assertThat(fieldAnnotation.annotationType()).isEqualTo(new TypeIdentifier(VariableAnnotation.class));

        AnnotationDescription description = fieldAnnotation.description();
        assertThat(description.asText())
                .contains(
                        "string=af",
                        "arrayString=bf",
                        "number=13",
                        "clz=Ljava/lang/reflect/Field;",
                        "arrayClz=[Ljava/lang/Object;, Ljava/lang/Object;]",
                        "enumValue=DUMMY1",
                        "annotation=Ljava/lang/Deprecated;[...]"
                );

        assertThat(description.textOf("arrayString")).isEqualTo("bf");
    }

    @Test
    void メソッドに付与されているアノテーションと記述が取得できる() throws Exception {
        TypeByteCode actual = exercise(Annotated.class);

        List<MethodByteCode> instanceMethodByteCodes = actual.instanceMethodByteCodes();
        MethodAnnotation methodAnnotation = instanceMethodByteCodes.stream()
                .filter(e -> e.method().declaration().asSignatureSimpleText().equals("method()"))
                .flatMap(e -> e.annotatedMethods().stream())
                // 今はアノテーション1つなのでこれでOK
                .findFirst().orElseThrow(AssertionError::new);

        assertThat(methodAnnotation.annotationType().fullQualifiedName()).isEqualTo(VariableAnnotation.class.getTypeName());

        AnnotationDescription description = methodAnnotation.description();
        assertThat(description.asText())
                .contains(
                        "string=am",
                        "arrayString=[bm1, bm2]",
                        "number=23",
                        "clz=Ljava/lang/reflect/Method;",
                        "enumValue=DUMMY2"
                );
    }

    @Test
    void クラス定義に使用している型が取得できる() throws Exception {
        TypeByteCode actual = exercise(ClassDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(ClassAnnotation.class),
                        new TypeIdentifier(SuperClass.class),
                        new TypeIdentifier(ImplementA.class),
                        new TypeIdentifier(ImplementB.class),
                        new TypeIdentifier(GenericsParameter.class)
                );

        ParameterizedType parameterizedSuperType = actual.parameterizedSuperType();
        assertThat(parameterizedSuperType)
                .extracting(
                        ParameterizedType::asSimpleText,
                        parameterizedType -> parameterizedSuperType.typeIdentifier()
                        )
                .containsExactly(
                        "SuperClass<Integer, Long>",
                        new TypeIdentifier(SuperClass.class)
                );
    }

    @Test
    void フィールド定義に使用している型が取得できる() throws Exception {
        TypeByteCode actual = exercise(FieldDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(stub.domain.model.relation.field.FieldAnnotation.class),
                        new TypeIdentifier(StaticField.class),
                        new TypeIdentifier(InstanceField.class),
                        new TypeIdentifier(GenericField.class),
                        new TypeIdentifier(ArrayField.class.getName() + "[]"),
                        new TypeIdentifier(ArrayField.class),
                        new TypeIdentifier(ReferenceConstantOwnerAtFieldDefinition.class),
                        new TypeIdentifier(ReferenceConstantAtFieldDefinition.class)
                );
    }

    @Test
    void メソッドでifやswitchを使用していると検出できる() throws Exception {
        TypeByteCode actual = exercise(DecisionClass.class);

        List<MethodByteCode> methodByteCodes = actual.instanceMethodByteCodes();

        assertThat(methodByteCodes)
                .extracting(
                        methodByteCode -> methodByteCode.methodDeclaration.asSignatureSimpleText(),
                        methodByteCode -> methodByteCode.method().decisionNumber().asText())
                .containsExactlyInAnyOrder(
                        tuple("分岐なしメソッド()", "0"),
                        tuple("ifがあるメソッド()", "1"),
                        tuple("switchがあるメソッド()", "1"),
                        // forは ifeq と goto で構成されるのでifと区別つかない
                        tuple("forがあるメソッド()", "2")
                );
    }

    @Test
    void enumで使用している型が取得できる() throws Exception {
        TypeByteCode actual = exercise(EnumDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(EnumField.class),
                        new TypeIdentifier(ConstructorArgument.class),
                        new TypeIdentifier(ClassReference.class)
                );
    }

    @ParameterizedTest
    @MethodSource
    void enumの特徴づけに必要な情報が取得できる(Class<?> clz, boolean hasMethod, boolean hasField, boolean canExtend) throws Exception {
        TypeByteCode actual = exercise(clz);

        assertThat(actual)
                .extracting(
                        TypeByteCode::isEnum,
                        TypeByteCode::hasInstanceMethod,
                        TypeByteCode::hasField,
                        TypeByteCode::canExtend
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

    private TypeByteCode exercise(Class<?> definitionClass) throws URISyntaxException {
        Path path = Paths.get(definitionClass.getResource(definitionClass.getSimpleName().concat(".class")).toURI());

        AsmByteCodeFactory sut = new AsmByteCodeFactory();
        return sut.analyze(new ByteCodeSource(path));
    }
}
