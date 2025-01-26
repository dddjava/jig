package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.sources.CodeSourcePaths;
import org.dddjava.jig.domain.model.sources.SourcePaths;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.classsources.BinarySourcePaths;
import org.dddjava.jig.infrastructure.filesystem.LocalClassFileSourceReader;
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

        LocalClassFileSourceReader sut = new LocalClassFileSourceReader();
        Sources source = sut.readSources(sourcePaths);

        assertTrue(source.nothingBinarySource());
        assertTrue(source.nothingTextSource());
    }
}