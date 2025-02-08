package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.dddjava.jig.infrastructure.asm.ut.field.MySutClass;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * FieldVisitorはClassVisitor経由でテストする
 */
class AsmFieldVisitorTest {

    @Test
    void JigFieldHeaderでJavaで書いたまま取れる() throws IOException {
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

    private static JigMemberBuilder 準備(Class<?> sutClass) throws IOException {
        AsmClassVisitor visitor = new AsmClassVisitor();
        new ClassReader(sutClass.getName()).accept(visitor, 0);
        return visitor.jigTypeBuilder();
    }

    void assertFieldSimpleNameWithGenerics(String expected, Optional<JigFieldHeader> actual) {
        assertEquals(expected, actual.orElseThrow().jigTypeReference().simpleNameWithGenerics());
    }
}