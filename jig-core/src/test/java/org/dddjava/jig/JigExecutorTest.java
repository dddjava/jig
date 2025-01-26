package org.dddjava.jig;

import org.dddjava.jig.domain.model.sources.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@JigServiceTest
class JigExecutorTest {

    @Test
    void name(Configuration configuration, SourcePaths SourcePaths) {
        var actual = JigExecutor.execute(
                configuration,
                SourcePaths
        );

        for (HandleResult handleResult : actual) {
            assertFalse(handleResult.failure(), () -> handleResult.toString());
        }
    }
}