package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceBasePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceBasePaths;
import org.dddjava.jig.infrastructure.filesystem.ClassOrJavaSourceCollector;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileSourcesFactoryTest {

    @Test
    void 読み取れないパスが指定されていてもエラーにならない() {
        SourceBasePaths sourceBasePaths = new SourceBasePaths(
                new ClassSourceBasePaths(Collections.singletonList(Paths.get("invalid-binary-path"))),
                new JavaSourceBasePaths(Collections.singletonList(Paths.get("invalid-text-path")))
        );

        ClassOrJavaSourceCollector sut = new ClassOrJavaSourceCollector();
        Sources source = sut.collectSources(sourceBasePaths);

        assertTrue(source.emptyClassSources());
        assertTrue(source.emptyJavaSources());
    }
}