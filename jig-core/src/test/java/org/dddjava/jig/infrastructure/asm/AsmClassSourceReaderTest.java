package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.type.*;
import org.dddjava.jig.domain.model.data.types.JigAnnotationData;
import org.dddjava.jig.domain.model.data.types.JigBaseTypeData;
import org.dddjava.jig.infrastructure.asm.ut.MyClass;
import org.dddjava.jig.infrastructure.asm.ut.MyGenericsMadnessInterface;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import stub.domain.model.MemberAnnotatedClass;
import stub.domain.model.annotation.RuntimeRetainedAnnotation;
import stub.domain.model.category.RichEnum;
import stub.domain.model.category.SimpleEnum;
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
import stub.domain.model.type.HogeRepository;
import stub.domain.model.type.SimpleNumber;
import stub.misc.DecisionClass;
import testing.TestSupport;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AsmClassSourceReaderTest {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AsmClassSourceReaderTest.class);

    @Nested
    class クラス {
        @Test
        void JigTypeDataの取得() throws IOException {
            AsmClassVisitor visitor = new AsmClassVisitor();
            new ClassReader(MyClass.class.getName()).accept(visitor, 0);

            var typeData = visitor.jigTypeHeader();

            assertEquals("MyClass", typeData.simpleName());
            assertEquals("org.dddjava.jig.infrastructure.asm.ut.MyClass", typeData.fqn());
            assertEquals("MyClass<X, Y>", typeData.simpleNameWithGenerics());

            // MyDeclarationAnnotationForSourceは含まれない
            assertEquals(List.of("MyDeclarationAnnotationForClass", "MyDeclarationAnnotationForRuntime"), typeData.jigTypeAttributeData()
                    .declarationAnnotationList().stream().map(JigAnnotationData::simpleTypeName).toList());

            assertEquals("MySuperClass", typeData.superType().orElseThrow().simpleName());
            assertEquals("MySuperClass<Integer, X, Long>", typeData.superType().orElseThrow().simpleNameWithGenerics());
            assertEquals("org.dddjava.jig.infrastructure.asm.ut.MySuperClass<java.lang.Integer, X, java.lang.Long>", typeData.superType().orElseThrow().fqnWithGenerics());
            assertEquals(List.of("MyInterface", "MyInterface2"), typeData.interfaceTypeList().stream().map(JigBaseTypeData::simpleName).toList());
            assertEquals(List.of("MyInterface<Y, String>", "MyInterface2<String, Y>"),
                    typeData.interfaceTypeList().stream().map(JigBaseTypeData::simpleNameWithGenerics).toList());
        }

        @Test
        void JigTypeDataの取得_やりすぎばん() throws IOException {
            AsmClassVisitor visitor = new AsmClassVisitor();
            new ClassReader(MyGenericsMadnessInterface.class.getName()).accept(visitor, 0);

            var typeData = visitor.jigTypeHeader();
            assertEquals("MyGenericsMadnessInterface", typeData.simpleName());
            assertEquals("MyGenericsMadnessInterface<T extends List>", typeData.simpleNameWithGenerics());
            // とれるようにしたいけどとりあえずはいいかなと
            //assertEquals("MyGenericsMadnessInterface<T extends List<Predicate<T>>", typeData.simpleNameWithGenerics());

            // interfaceのsuperは入ってないとおもってたけどObjectが入っている
            assertEquals("Object", typeData.superType().orElseThrow().simpleName());

            // interfaceのextendsはinterfaceで取れる模様
            assertEquals(List.of("Consumer<Consumer>"),
                    typeData.interfaceTypeList().stream().map(JigBaseTypeData::simpleNameWithGenerics).toList());
        }

        @Test
        void クラス定義に使用している型が取得できる() throws Exception {
            JigType actual = JigType構築(ClassDefinition.class);

            TypeIdentifiers identifiers = actual.usingTypes();
            assertThat(identifiers.list())
                    .contains(
                            TypeIdentifier.from(ClassAnnotation.class),
                            TypeIdentifier.from(SuperClass.class),
                            TypeIdentifier.from(ImplementA.class),
                            TypeIdentifier.from(ImplementB.class),
                            TypeIdentifier.from(GenericsParameter.class)
                    );

            ParameterizedType parameterizedSuperType = actual.typeDeclaration().superType();
            assertThat(parameterizedSuperType)
                    .extracting(
                            ParameterizedType::asSimpleText,
                            ParameterizedType::typeIdentifier
                    )
                    .containsExactly(
                            "SuperClass<Integer, Long>",
                            TypeIdentifier.from(SuperClass.class)
                    );
        }

        @Test
        void インタフェース定義に使用している型が取得できる() throws Exception {
            JigType actual = JigType構築(InterfaceDefinition.class);

            TypeIdentifiers identifiers = actual.usingTypes();
            assertThat(identifiers.list())
                    .contains(
                            TypeIdentifier.from(ClassAnnotation.class),
                            TypeIdentifier.from(Comparable.class),
                            TypeIdentifier.from(GenericsParameter.class)
                    );

            ParameterizedType parameterizedType = actual.typeDeclaration().interfaceTypes().list().get(0);
            assertThat(parameterizedType)
                    .extracting(
                            ParameterizedType::asSimpleText,
                            ParameterizedType::typeIdentifier
                    )
                    .containsExactly(
                            "Comparable<GenericsParameter>",
                            TypeIdentifier.from(Comparable.class)
                    );
        }

        @Test
        void enumで使用している型が取得できる() throws Exception {
            JigType actual = JigType構築(EnumDefinition.class);

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
            JigType actual = JigType構築(targetType);
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

    @Nested
    class フィールド {
        @Test
        void フィールドに付与されているアノテーションと記述が取得できる() throws Exception {
            JigType actual = JigType構築(MemberAnnotatedClass.class);

            JigFields jigFields = actual.instanceMember().instanceFields();

            FieldAnnotation fieldAnnotation = jigFields.list().stream()
                    .filter(e -> e.fieldDeclaration().nameText().equals("field"))
                    .findFirst()
                    .flatMap(jigField -> jigField.fieldAnnotations().list().stream().findFirst())
                    .orElseThrow(AssertionError::new);

            assertEquals(TypeIdentifier.from(VariableAnnotation.class), fieldAnnotation.annotationType());

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
        void フィールド定義に使用している型が取得できる() throws Exception {
            JigType jigType = JigType構築(FieldDefinition.class);

            FieldDeclarations fieldDeclarations = jigType.instanceMember().fieldDeclarations();
            String fieldsText = fieldDeclarations.toSignatureText();
            assertEquals("[InstanceField instanceField, List genericFields, ArrayField[] arrayFields, Object obj]", fieldsText);

            TypeIdentifiers identifiers = jigType.usingTypes();
            assertThat(identifiers.list())
                    .contains(
                            TypeIdentifier.from(List.class),
                            TypeIdentifier.from(stub.domain.model.relation.field.FieldAnnotation.class),
                            TypeIdentifier.from(StaticField.class),
                            TypeIdentifier.from(InstanceField.class),
                            TypeIdentifier.from(GenericField.class),
                            TypeIdentifier.valueOf(ArrayField.class.getName() + "[]"),
                            TypeIdentifier.from(ArrayField.class),
                            TypeIdentifier.from(ReferenceConstantOwnerAtFieldDefinition.class),
                            TypeIdentifier.from(ReferenceConstantAtFieldDefinition.class)
                    );
        }

    }

    @Nested
    class メソッド {
        @Test
        void メソッドに付与されているアノテーションと記述が取得できる() throws Exception {
            JigType actual = JigType構築(MemberAnnotatedClass.class);

            JigMethod method = resolveMethodBySignature(actual, new MethodSignature("method"));
            MethodAnnotation methodAnnotation = method.methodAnnotations().list().get(0);

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
        void 戻り値のジェネリクスが取得できる() throws Exception {
            JigType actual = JigType構築(InterfaceDefinition.class);

            TypeIdentifiers identifiers = actual.usingTypes();
            assertThat(identifiers.list())
                    .contains(
                            TypeIdentifier.from(List.class),
                            TypeIdentifier.from(String.class)
                    );

            MethodReturn methodReturn = resolveMethodBySignature(actual, new MethodSignature("parameterizedListMethod"))
                    .declaration().methodReturn();
            ParameterizedType parameterizedType = methodReturn.parameterizedType();

            assertThat(parameterizedType.asSimpleText()).isEqualTo("List<String>");
        }

        @Test
        void resolveArgumentGenerics() {
            JigType jigType = JigType構築(ResolveArgumentGenerics.class);

            JigMethod actual = resolveMethodByName(jigType, "method");

            assertThat(actual.declaration().methodSignature().asText())
                    .isEqualTo("method(java.util.List<java.lang.String>)");
        }

        @Test
        void メソッドでifやswitchを使用していると検出できる() throws Exception {
            JigType actual = JigType構築(DecisionClass.class);

            JigMethods jigMethods = actual.instanceMethods();
            assertThat(jigMethods.list())
                    .extracting(
                            method -> method.declaration().methodSignature().asSimpleText(),
                            method -> method.decisionNumber().asText())
                    .containsExactlyInAnyOrder(
                            tuple("分岐なしメソッド()", "0"),
                            tuple("ifがあるメソッド()", "1"),
                            tuple("switchがあるメソッド()", "1"),
                            // forは ifeq と goto で構成されるある意味での分岐
                            tuple("forがあるメソッド()", "1")
                    );
        }
    }

    private JigType JigType構築(Class<?> clz) {
        try {
            var classSource = TestSupport.getClassSource(clz);

            AsmClassSourceReader sut = new AsmClassSourceReader();
            return sut.classDeclaration(classSource).orElseThrow().build();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    JigMethod resolveMethodBySignature(JigType jigType, MethodSignature methodSignature) {
        return jigType.allJigMethodStream()
                .filter(jigMethod -> jigMethod.declaration().methodSignature().isSame(methodSignature))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    private JigMethod resolveMethodByName(JigType jigType, String name) {
        return jigType.allJigMethodStream()
                .filter(jigMethod -> jigMethod.name().equals(name))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    static class ResolveArgumentGenerics {
        public void method(List<String> list) {
        }
    }
}
