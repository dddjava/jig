package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.infrastructure.asm.ut.field.MyEnumFieldSut;
import org.dddjava.jig.infrastructure.asm.ut.field.MySutClass;
import org.junit.jupiter.api.Test;
import stub.domain.model.MemberAnnotatedClass;
import stub.domain.model.relation.annotation.VariableAnnotation;
import testing.TestSupport;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldVisitorはClassVisitor経由でテストする
 */
class AsmFieldVisitorTest {

    @Test
    void JigFieldHeaderでJavaで書いたまま取れる() {
        var members = TestSupport.buildJigType(MySutClass.class).jigTypeMembers();
        List<String> actual = members.jigFields().stream()
                .map(JigField::simpleNameWithGenerics)
                .sorted().toList();

        assertEquals(List.of(
                        "List<BigDecimal> genericField",
                        "List<BigDecimal[]> genericArrayField",
                        "List<T> genericTypeVariableField",
                        "List<T[]> genericTypeVariableArrayField",
                        "String stringField",
                        "String[] stringArrayField",
                        "T typeVariableField",
                        "T[] typeVariableArrayField",
                        "T[][] typeVariable2DArrayField",
                        "byte primitiveField",
                        "int[] primitiveArrayField"
                ),
                actual);
    }

    @Test
    void enumフィールドのテスト() {
        var members = TestSupport.buildJigType(MyEnumFieldSut.class).jigTypeMembers();

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
        var members = TestSupport.buildJigType(MemberAnnotatedClass.class).jigTypeMembers();
        JigFieldHeader field = members.findFieldByName("field").orElseThrow();

        JigAnnotationReference sut = field.jigFieldAttribute().declarationAnnotations().stream().findFirst().orElseThrow();

        assertEquals(TypeIdentifier.from(VariableAnnotation.class), sut.id());

        assertThat(sut.asText())
                .contains(
                        "string=af",
                        "arrayString=bf",
                        "number=13",
                        "clz=Field",
                        "arrayClz={Object, Object}",
                        "enumValue=UseInAnnotation.DUMMY1",
                        "annotation=@Deprecated(...)"
                );

        assertThat(sut.elementTextOf("arrayString").orElseThrow()).isEqualTo("bf");
    }
}