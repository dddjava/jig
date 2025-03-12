package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigServiceTest
class JigExecutorTest {

    @Test
    void name(Configuration configuration, SourceBasePaths SourceBasePaths) {
        var actual = JigExecutor.execute(
                configuration,
                SourceBasePaths
        );

        List<JigDocument> actualDocuments = actual.stream().map(handleResult -> handleResult.jigDocument).toList();
        // canonicalのすべてが処理されている
        assertTrue(actualDocuments.containsAll(JigDocument.canonical()), actualDocuments::toString);

        for (HandleResult handleResult : actual) {
            assertFalse(handleResult.failure(), () -> handleResult.toString());
        }
    }
}