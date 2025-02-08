package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.dddjava.jig.infrastructure.asm.ut.field.MyEnumFieldSut;
import org.dddjava.jig.infrastructure.asm.ut.field.MySutClass;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import stub.domain.model.MemberAnnotatedClass;
import stub.domain.model.relation.FieldDefinition;
import stub.domain.model.relation.annotation.VariableAnnotation;
import stub.domain.model.relation.field.*;
import testing.TestSupport;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldVisitorはClassVisitor経由でテストする
 */
class AsmFieldVisitorTest {

    @Test
    void JigFieldHeaderでJavaで書いたまま取れる() {
        var jigMemberBuilder = 準備(MySutClass.class);
        var members = jigMemberBuilder.buildMember();

        assertFieldSimpleNameWithGenerics("byte", members.findFieldByName("primitiveField"));
        assertFieldSimpleNameWithGenerics("int[]", members.findFieldByName("primitiveArrayField"));
        assertFieldSimpleNameWithGenerics("String", members.findFieldByName("stringField"));
        assertFieldSimpleNameWithGenerics("String[]", members.findFieldByName("stringArrayField"));
        assertFieldSimpleNameWithGenerics("List<BigDecimal>", members.findFieldByName("genericField"));
        assertFieldSimpleNameWithGenerics("List<BigDecimal[]>", members.findFieldByName("genericArrayField"));
        assertFieldSimpleNameWithGenerics("List<T>", members.findFieldByName("genericTypeVariableField"));
        assertFieldSimpleNameWithGenerics("List<T[]>", members.findFieldByName("genericTypeVariableArrayField"));
        assertFieldSimpleNameWithGenerics("T", members.findFieldByName("typeVariableField"));
        assertFieldSimpleNameWithGenerics("T[]", members.findFieldByName("typeVariableArrayField"));
        assertFieldSimpleNameWithGenerics("T[][]", members.findFieldByName("typeVariable2DArrayField"));
    }

    private static JigMemberBuilder 準備(Class<?> sutClass) {
        try {
            AsmClassVisitor visitor = new AsmClassVisitor();
            new ClassReader(sutClass.getName()).accept(visitor, 0);
            return visitor.jigTypeBuilder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void assertFieldSimpleNameWithGenerics(String expected, Optional<JigFieldHeader> actual) {
        assertEquals(expected, actual.orElseThrow().jigTypeReference().simpleNameWithGenerics());
    }

    @Test
    void enumフィールドのテスト() {
        var jigMemberBuilder = 準備(MyEnumFieldSut.class);
        var members = jigMemberBuilder.buildMember();

        List<String> enumConstantNames = members.enumConstantNames();
        assertEquals(List.of("通常の列挙値1", "通常の列挙値2", "Deprecatedな列挙値"), enumConstantNames,
                "enumの列挙として記述された以外のフィールドが含まれていないこと");

        JigField normalConstant = members.findFieldByName("通常の列挙値1")
                .map(JigField::from).orElseThrow();
        assertEquals("通常の列挙値1", normalConstant.nameText());
        assertFalse(normalConstant.isDeprecated());

        JigField deprecatedConstant = members.findFieldByName("Deprecatedな列挙値")
                .map(JigField::from).orElseThrow();
        assertEquals("Deprecatedな列挙値", deprecatedConstant.nameText());
        assertTrue(deprecatedConstant.isDeprecated());
    }

    @Test
    void フィールドに付与されているアノテーションと記述が取得できる() throws Exception {
        JigType actual = TestSupport.buildJigType(MemberAnnotatedClass.class);

        JigFields jigFields = actual.instanceJigFields();

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
        JigType jigType = TestSupport.buildJigType(FieldDefinition.class);

        String fieldsText = jigType.jigTypeMembers().instanceFieldsSimpleText();
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