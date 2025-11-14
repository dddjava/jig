package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.junit.jupiter.api.Test;
import testing.JigTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JigTest
class JigExecutorTest {

    @Test
    void 出力対象がある場合に実行できる(Configuration configuration, SourceBasePaths sourceBasePaths) {
        var actual = JigExecutor.standard(
                configuration,
                sourceBasePaths
        ).listResult();

        List<JigDocument> actualDocuments = actual.stream().map(handleResult -> handleResult.jigDocument()).toList();
        // canonicalのすべてが処理されている
        assertTrue(actualDocuments.containsAll(JigDocument.canonical()), actualDocuments::toString);

        // すべて失敗していない（success or skip）であること
        assertAll(actual.stream().map(actualResult ->
                () -> assertFalse(((HandleResultImpl)actualResult).failure(), () -> actualResult.toString())
        ));
    }
}