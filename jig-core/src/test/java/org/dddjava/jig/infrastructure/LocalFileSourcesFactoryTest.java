package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.implementation.source.SourcePaths;
import org.dddjava.jig.domain.model.implementation.source.Sources;
import org.dddjava.jig.domain.model.implementation.source.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.implementation.source.code.CodeSourcePaths;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileSourcesFactoryTest {

    @Test
    void 読み取れないパスが指定されていてもエラーにならない() {
        SourcePaths sourcePaths = new SourcePaths(
                new BinarySourcePaths(Collections.singletonList(Paths.get("invalid-binary-path"))),
                new CodeSourcePaths(Collections.singletonList(Paths.get("invalid-text-path")))
        );

        LocalFileSourceReader sut = new LocalFileSourceReader();
        Sources source = sut.readSources(sourcePaths);

        assertTrue(source.classSources().list().isEmpty());
        assertTrue(source.aliasSource().javaSources().list().isEmpty());
        assertTrue(source.aliasSource().packageInfoSources().list().isEmpty());
    }
}