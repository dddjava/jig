package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.sources.Sources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testing.JigTestExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(JigTestExtension.class)
class PackageInfoReaderTest {

    @Test
    void test(Sources sources) throws Exception {
        List<Path> list = sources.javaSources().paths();
        assertFalse(list.isEmpty(), "0件だったら " + JigTestExtension.class + " がおかしい");
        Path targetPath = list.stream()
                .filter(e -> e.endsWith(Paths.get("domain", "model", "package-info.java"))).findAny()
                .orElseThrow(AssertionError::new);

        JavaparserReader sut = new JavaparserReader(null);
        var term = sut.parsePackageInfoJavaFile(targetPath).orElseThrow();

        assertEquals("スタブドメインモデル", term.title());
    }
}