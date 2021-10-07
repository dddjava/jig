package org.dddjava.jig.infrastructure.asm;

import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AsmJdk17Test {

    @Test
    void recordが読める() throws Exception {
        Path path = Paths.get(TestSupport.resourceRootURI()).resolve("jdk17").resolve("MyRecord.class");

        AsmFactReader sut = new AsmFactReader();
        sut.typeByteCode(TestSupport.newClassSource(path));
    }
}
