package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigsource.analyzed.MethodFact;
import org.dddjava.jig.domain.model.jigsource.analyzed.TypeFact;
import org.dddjava.jig.domain.model.parts.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.parts.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.parts.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.parts.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.method.MethodReturn;
import org.dddjava.jig.domain.model.parts.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifiers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.Annotated;
import stub.domain.model.category.*;
import stub.domain.model.relation.ClassDefinition;
import stub.domain.model.relation.EnumDefinition;
import stub.domain.model.relation.FieldDefinition;
import stub.domain.model.relation.InterfaceDefinition;
import stub.domain.model.relation.annotation.VariableAnnotation;
import stub.domain.model.relation.clz.*;
import stub.domain.model.relation.enumeration.ClassReference;
import stub.domain.model.relation.enumeration.ConstructorArgument;
import stub.domain.model.relation.enumeration.EnumField;
import stub.domain.model.relation.field.*;
import stub.misc.DecisionClass;
import testing.TestSupport;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsmFactReaderTest {

    @Test
    void JDK11でコンパイルされたクラス() throws IOException {
        Path path = Paths.get(TestSupport.resourceRootURI()).resolve("jdk11").resolve("CompiledJdk11NestingClass.class");

        AsmFactReader sut = new AsmFactReader();
        sut.typeByteCode(TestSupport.newClassSource(path));
    }

    @Test
    void フィールドに付与されているアノテーションと記述が取得できる() throws Exception {
        TypeFact actual = exercise(Annotated.class);

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
        TypeFact actual = exercise(Annotated.class);

        List<MethodFact> instanceMethodFacts = actual.instanceMethodFacts();
        MethodAnnotation methodAnnotation = instanceMethodFacts.stream()
                .filter(e -> e.createMethod().declaration().asSignatureSimpleText().equals("method()"))
                .flatMap(e -> e.annotatedMethods().list().stream())
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
        TypeFact actual = exercise(ClassDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(ClassAnnotation.class),
                        new TypeIdentifier(SuperClass.class),
                        new TypeIdentifier(ImplementA.class),
                        new TypeIdentifier(ImplementB.class),
                        new TypeIdentifier(GenericsParameter.class)
                );

        ParameterizedType parameterizedSuperType = actual.superType();
        assertThat(parameterizedSuperType)
                .extracting(
                        ParameterizedType::asSimpleText,
                        ParameterizedType::typeIdentifier
                )
                .containsExactly(
                        "SuperClass<Integer, Long>",
                        new TypeIdentifier(SuperClass.class)
                );
    }

    @Test
    void インタフェース定義に使用している型が取得できる() throws Exception {
        TypeFact actual = exercise(InterfaceDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(ClassAnnotation.class),
                        new TypeIdentifier(Comparable.class),
                        new TypeIdentifier(GenericsParameter.class)
                );

        ParameterizedType parameterizedType = actual.interfaceTypes().get(0);
        assertThat(parameterizedType)
                .extracting(
                        ParameterizedType::asSimpleText,
                        ParameterizedType::typeIdentifier
                )
                .containsExactly(
                        "Comparable<GenericsParameter>",
                        new TypeIdentifier(Comparable.class)
                );
    }

    @Test
    void 戻り値のジェネリクスが取得できる() throws Exception {
        TypeFact actual = exercise(InterfaceDefinition.class);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(List.class),
                        new TypeIdentifier(String.class)
                );

        List<MethodFact> instanceMethodFacts = actual.instanceMethodFacts();
        MethodFact methodFact = instanceMethodFacts.stream()
                .filter(e -> e.createMethod().declaration().asSignatureSimpleText().equals("parameterizedListMethod()"))
                .findFirst().orElseThrow(AssertionError::new);
        JigMethod method = methodFact.createMethod();

        MethodReturn methodReturn = method.declaration().methodReturn();

        ParameterizedType parameterizedType =
                methodReturn.parameterizedType();

        assertThat(parameterizedType.asSimpleText()).isEqualTo("List<String>");
    }

    @Test
    void フィールド定義に使用している型が取得できる() throws Exception {
        TypeFact actual = exercise(FieldDefinition.class);

        FieldDeclarations fieldDeclarations = actual.fieldDeclarations();
        String fieldsText = fieldDeclarations.toSignatureText();
        assertEquals("[InstanceField instanceField, List genericFields, ArrayField[] arrayFields, Object obj]", fieldsText);

        TypeIdentifiers identifiers = actual.useTypes();
        assertThat(identifiers.list())
                .contains(
                        new TypeIdentifier(List.class),
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
        TypeFact actual = exercise(DecisionClass.class);

        List<MethodFact> methodFacts = actual.instanceMethodFacts();

        assertThat(methodFacts)
                .extracting(
                        methodByteCode -> methodByteCode.methodIdentifier().methodSignature().asSimpleText(),
                        methodByteCode -> methodByteCode.decisionNumber().asText())
                .containsExactlyInAnyOrder(
                        tuple("分岐なしメソッド()", "0"),
                        tuple("ifがあるメソッド()", "1"),
                        tuple("switchがあるメソッド()", "1"),
                        // forは ifeq と goto で構成されるある意味での分岐
                        tuple("forがあるメソッド()", "1")
                );
    }

    @Test
    void enumで使用している型が取得できる() throws Exception {
        TypeFact actual = exercise(EnumDefinition.class);

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
    void enumの特徴づけに必要な情報が取得できる(Class<?> clz, boolean hasMethod, boolean hasField) throws Exception {
        TypeFact actual = exercise(clz);

        assertThat(actual)
                .extracting(
                        typeFact -> !typeFact.instanceMethodFacts().isEmpty(),
                        typeFact -> !typeFact.fieldDeclarations().empty()
                )
                .containsExactly(
                        hasMethod,
                        hasField
                );
    }

    static Stream<Arguments> enumの特徴づけに必要な情報が取得できる() {
        return Stream.of(
                Arguments.of(SimpleEnum.class, false, false),
                Arguments.of(BehaviourEnum.class, true, false),
                Arguments.of(ParameterizedEnum.class, false, true),
                Arguments.of(PolymorphismEnum.class, false, false),
                Arguments.of(RichEnum.class, true, true));
    }

    private TypeFact exercise(Class<?> definitionClass) throws URISyntaxException, IOException {
        Path path = Paths.get(definitionClass.getResource(definitionClass.getSimpleName().concat(".class")).toURI());

        AsmFactReader sut = new AsmFactReader();
        return sut.typeByteCode(TestSupport.newClassSource(path));
    }
}
