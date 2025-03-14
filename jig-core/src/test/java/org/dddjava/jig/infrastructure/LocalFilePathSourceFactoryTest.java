package org.dddjava.jig.infrastructure;

import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.domain.model.sources.PathSource;
import org.dddjava.jig.domain.model.sources.SourceBasePath;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.infrastructure.javaproductreader.ClassOrJavaSourceCollector;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFilePathSourceFactoryTest {

    @Test
    void 読み取れないパスが指定されていてもエラーにならない() {
        var invalidClassPath = Path.of("invalid-binary-path");
        var invalidJavaPath = Path.of("invalid-text-path");
        SourceBasePaths sourceBasePaths = new SourceBasePaths(
                new SourceBasePath(Collections.singletonList(invalidClassPath)),
                new SourceBasePath(Collections.singletonList(invalidJavaPath))
        );

        var jigEventRepository = Mockito.spy(new JigEventRepository());
        ClassOrJavaSourceCollector sut = new ClassOrJavaSourceCollector(jigEventRepository);
        PathSource source = sut.collectSources(sourceBasePaths);

        assertTrue(source.emptyClassSources());
        assertTrue(source.emptyJavaSources());

        Mockito.verify(jigEventRepository).register指定されたパスが存在しない(invalidClassPath);
        Mockito.verify(jigEventRepository).register指定されたパスが存在しない(invalidJavaPath);
        Mockito.verifyNoMoreInteractions(jigEventRepository);
    }
}