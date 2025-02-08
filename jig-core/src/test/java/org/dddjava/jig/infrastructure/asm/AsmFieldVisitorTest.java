package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.infrastructure.asm.ut.MySutClass;
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
        AsmClassVisitor visitor = new AsmClassVisitor();
        new ClassReader(MySutClass.class.getName()).accept(visitor, 0);

        var jigMemberBuilder = visitor.jigTypeBuilder();
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

    void assertFieldSimpleNameWithGenerics(String expected, Optional<JigFieldHeader> actual) {
        assertEquals(expected, actual.orElseThrow().jigTypeReference().simpleNameWithGenerics());
    }
}