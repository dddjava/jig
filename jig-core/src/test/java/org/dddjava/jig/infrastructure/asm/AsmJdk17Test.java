package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.TypeKind;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsmJdk17Test {

    @Test
    void recordが読める() throws Exception {
        Path path = Paths.get(TestSupport.resourceRootURI()).resolve("jdk17").resolve("MyRecord.class");

        AsmFactReader sut = new AsmFactReader();
        JigType jigType = sut.typeByteCode(TestSupport.newClassSource(path)).orElseThrow().build();

        TypeKind typeKind = jigType.typeKind();
        assertEquals(typeKind, TypeKind.レコード型);
    }
}
