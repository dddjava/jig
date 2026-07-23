package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 「解析対象がある場合に全ドキュメントが生成される」ことは
 * {@code org.dddjava.jig.contract.ShowcaseSiteContractTest} が実際の代表プロジェクトで検証している。
 * ここでは解析対象が無いという境界条件だけを見る。
 */
class JigExecutorTest {

    @Test
    void 出力対象がない場合にエラーにならず正常終了する(@TempDir Path outputDirectory) {
        var configuration = Configuration.from(new JigSettings(
                outputDirectory, Optional.empty(), JigDocument.canonical(), Locale.JAPANESE));

        var actual = JigExecutor.standard(
                configuration,
                new SourceBasePaths(new SourceBasePath(Collections.emptyList()), new SourceBasePath(Collections.emptyList()))
        ).listResult();

        List<JigDocument> actualDocuments = actual.stream().map(handleResult -> handleResult.jigDocument()).toList();
        // canonicalのすべてが処理されている
        assertTrue(actualDocuments.containsAll(JigDocument.canonical()), actualDocuments::toString);
    }
}