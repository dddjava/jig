package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testing.TestSupport;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultJigRepositoryFactoryLifecycleTest {

    @Test
    void 同じ設定から作った実行コンテキストは解析状態を共有しない(@TempDir Path outputDirectory) {
        var configuration = Configuration.from(new JigSettings(
                outputDirectory, Optional.empty(), JigDocument.canonical(), Locale.JAPANESE));
        var firstRepository = DefaultJigRepositoryFactory.init(configuration.newExecution()).createJigRepository(
                TestSupport.sourceLocationsFor("org/dddjava/jig/infrastructure/javaparser/ut"));
        assertFalse(firstRepository.fetchJigTypes().isEmpty());

        // 前回実行で収集した package-info の用語が、次回の解析結果に混ざらない。
        var previousTermId = new TermId("org.dddjava.jig.infrastructure.javaparser.ut.package_info_javadoc");
        assertTrue(firstRepository.fetchGlossary().terms().stream().anyMatch(term -> term.id().equals(previousTermId)));

        var secondRepository = DefaultJigRepositoryFactory.init(configuration.newExecution()).createJigRepository(
                TestSupport.sourceLocationsFor("org/dddjava/jig/application/ut/domain/model"));
        assertFalse(secondRepository.fetchGlossary().terms().stream().anyMatch(term -> term.id().equals(previousTermId)));
        assertTrue(firstRepository.fetchGlossary().terms().stream().anyMatch(term -> term.id().equals(previousTermId)));
    }
}
