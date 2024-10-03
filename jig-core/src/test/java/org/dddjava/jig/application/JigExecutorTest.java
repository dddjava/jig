package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@JigServiceTest
class JigExecutorTest {

    @Test
    void name(Configuration configuration, SourcePaths SourcePaths) {
        HandleResults handleResults = JigExecutor.executeInternal(
                configuration,
                SourcePaths
        );

        assertTrue(handleResults.completelySuccessful(), () -> handleResults.failures().toString());
    }
}