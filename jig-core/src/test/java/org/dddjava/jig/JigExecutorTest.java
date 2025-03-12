package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JigServiceTest
class JigExecutorTest {

    @Test
    void name(Configuration configuration, SourceBasePaths sourceBasePaths) {
        var actual = JigExecutor.execute(
                configuration,
                sourceBasePaths
        );

        List<JigDocument> actualDocuments = actual.stream().map(handleResult -> handleResult.jigDocument).toList();
        // canonicalのすべてが処理されている
        assertTrue(actualDocuments.containsAll(JigDocument.canonical()), actualDocuments::toString);

        // すべて失敗していない（success or skip）であること
        assertAll(actual.stream().map(actualResult ->
                () -> assertFalse(actualResult.failure(), () -> actualResult.toString())
        ));
    }
}