package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.sources.ReadStatus;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisStatementsReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import testing.TestSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultJigRepositoryFactoryLifecycleTest {

    @Test
    void 同じFactoryを再利用しても解析状態を共有しない(@TempDir Path outputDirectory) {
        var configuration = Configuration.from(new JigSettings(
                outputDirectory, Optional.empty(), JigDocument.canonical(), Locale.JAPANESE));
        var factory = DefaultJigRepositoryFactory.init(configuration);
        var firstRepository = factory.createJigRepository(
                TestSupport.sourceLocationsFor("org/dddjava/jig/infrastructure/javaparser/sut"));
        assertFalse(firstRepository.fetchJigTypes().isEmpty());

        // 前回実行で収集した package-info の用語が、次回の解析結果に混ざらない。
        var previousTermId = new TermId("org.dddjava.jig.infrastructure.javaparser.sut.package_info_javadoc");
        assertTrue(firstRepository.fetchGlossary().terms().stream().anyMatch(term -> term.id().equals(previousTermId)));

        var secondRepository = factory.createJigRepository(
                TestSupport.sourceLocationsFor("org/dddjava/jig/application/sut/domain/model"));
        assertFalse(secondRepository.fetchGlossary().terms().stream().anyMatch(term -> term.id().equals(previousTermId)));
        assertTrue(firstRepository.fetchGlossary().terms().stream().anyMatch(term -> term.id().equals(previousTermId)));
    }

    @Test
    void テキストソースのパース失敗をイベントとして記録する(@TempDir Path tempDirectory) throws IOException {
        Files.writeString(tempDirectory.resolve("Broken.java"), "class Broken {");
        var eventRepositoryReference = new AtomicReference<JigEventRepository>();
        var factory = new DefaultJigRepositoryFactory(
                new AsmClassSourceReader(),
                new JavaparserReader(),
                new MyBatisStatementsReader(),
                () -> {
                    var eventRepository = Mockito.spy(new JigEventRepository(Locale.JAPANESE));
                    eventRepositoryReference.set(eventRepository);
                    return new DefaultJigRepositoryFactory.AnalysisState(eventRepository, new OnMemoryGlossaryRepository());
                });
        var sourceLocations = TestSupport.sourceLocationsFor("org/dddjava/jig/infrastructure/javaparser/sut");
        var sourceBasePaths = new SourceBasePaths(
                sourceLocations.classFileBasePath(),
                new SourceBasePath(List.of(tempDirectory)));

        factory.createJigRepository(sourceBasePaths);

        Mockito.verify(eventRepositoryReference.get()).recordEvent(ReadStatus.テキストソース読み込み一部失敗);
    }
}
