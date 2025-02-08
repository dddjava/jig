package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.dddjava.jig.infrastructure.asm.ut.field.MyEnumFieldSut;
import org.dddjava.jig.infrastructure.asm.ut.field.MySutClass;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
}