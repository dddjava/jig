package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.TypeKind;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.MemberAnnotatedClass;
import stub.domain.model.annotation.RuntimeRetainedAnnotation;
import stub.domain.model.category.RichEnum;
import stub.domain.model.category.SimpleEnum;
import stub.domain.model.relation.ClassDefinition;
import stub.domain.model.relation.EnumDefinition;
import stub.domain.model.relation.InterfaceDefinition;
import stub.domain.model.relation.annotation.VariableAnnotation;
import stub.domain.model.relation.clz.*;
import stub.domain.model.relation.enumeration.ClassReference;
import stub.domain.model.relation.enumeration.ConstructorArgument;
import stub.domain.model.relation.enumeration.EnumField;
import stub.domain.model.type.HogeRepository;
import stub.domain.model.type.SimpleNumber;
import stub.misc.DecisionClass;
import testing.TestSupport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AsmClassSourceReaderTest {

    @Nested
    class クラス {

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

    @Nested
    class メソッド {
        @Test
        void メソッドに付与されているアノテーションと記述が取得できる() throws Exception {
            JigType actual = TestSupport.buildJigType(MemberAnnotatedClass.class);

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
            JigType actual = TestSupport.buildJigType(InterfaceDefinition.class);

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
            JigType jigType = TestSupport.buildJigType(ResolveArgumentGenerics.class);

            JigMethod actual = resolveMethodByName(jigType, "method");

            assertThat(actual.declaration().methodSignature().asText())
                    .isEqualTo("method(java.util.List<java.lang.String>)");
        }

        @Test
        void メソッドでifやswitchを使用していると検出できる() throws Exception {
            JigType actual = TestSupport.buildJigType(DecisionClass.class);

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
