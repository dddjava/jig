package org.dddjava.jig.adapter;

import io.micrometer.core.instrument.Metrics;
import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.adapter.documents.*;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JigDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentGenerator.class);

    private static final List<String> ASSET_FILES = List.of(
            "domain.js", "favicon.ico", "glossary.js", "inbound.js", "index.js",
            "insight.js", "jig-dom.js", "jig-glossary.js", "jig-mermaid.js",
            "list-output.js", "outbound.js", "package.js", "style.css", "types.js", "usecase.js"
    );

    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;
    private final List<JigDocumentAdapter> adapters;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();
        Path outputDir = jigDocumentContext.outputDirectory();

        this.adapters = List.of(
                new DomainModelAdapter(jigService, outputDir),
                new InsightAdapter(jigService, outputDir),
                new OutboundInterfaceAdapter(jigService, outputDir),
                new InboundInterfaceAdapter(jigService, outputDir),
                new UsecaseModelAdapter(jigService, outputDir),
                new ListOutputAdapter(jigService, outputDir),
                new GlossaryAdapter(jigService, outputDir),
                new PackageRelationAdapter(jigService, outputDir)
        );
    }

    public JigResult generate(JigRepository jigRepository) {
        prepareOutputDirectory();

        var handleResults = generateDocuments(jigRepository);

        generateIndex(handleResults);
        generateDebugHtml();
        generateAssets();
        return new JigResultData(handleResults, IndexAdapter.indexFilePath(outputDirectory));
    }

    private void prepareOutputDirectory() {
        createOutputDirectory(outputDirectory);
        createOutputDirectory(outputDirectory.resolve("assets"));
        createOutputDirectory(outputDirectory.resolve("data"));
    }

    private void createOutputDirectory(Path outputDirectory) {
        File file = outputDirectory.toFile();
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " is not Directory. Please review your settings.");
            }
            if (!file.canWrite()) {
                throw new IllegalStateException(file.getAbsolutePath() + " can not writable. Please specify another directory.");
            }
            return;
        }

        try {
            Files.createDirectories(outputDirectory);
            logger.info("[JIG] created {}", outputDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<HandleResult> generateDocuments(JigRepository jigRepository) {
        return jigDocuments
                .parallelStream()
                .map(jigDocument -> generateDocument(jigDocument, jigRepository))
                .toList();
    }

    private HandleResult generateDocument(JigDocument jigDocument, JigRepository jigRepository) {
        return Objects.requireNonNull(Metrics.timer("jig.document.time", "phase", jigDocument.name()).record(() -> {
            try {
                long startTime = System.currentTimeMillis();

                // HTMLコピー（Adapter固有の処理ではないため、Generator側で一元管理）
                Path htmlPath = JigDocumentWriter.writeHtml(jigDocument, outputDirectory);

                var outputFilePaths = adapters.stream()
                        .filter(adapter -> adapter.supportedDocument() == jigDocument)
                        .findFirst()
                        .map(adapter -> adapter.write(jigDocument, jigRepository))
                        .orElse(List.of());

                // HTMLのパス + データファイルのパスを結合
                var allPaths = Stream.concat(
                        Stream.of(htmlPath),
                        outputFilePaths.stream()
                ).toList();

                long takenTime = System.currentTimeMillis() - startTime;
                logger.info("[{}] completed: {} ms", jigDocument, takenTime);
                return HandleResult.withOutput(jigDocument, allPaths);
            } catch (Exception e) {
                // ドキュメント出力に失敗しても例外を伝播させない
                logger.warn("[{}] failed to write document.", jigDocument, e);
                return HandleResult.withException(jigDocument, e);
            }
        }));
    }

    private void generateDebugHtml() {
        try {
            JigDocumentWriter.copyResourceTo("templates/debug.html", outputDirectory.resolve("debug.html"));
        } catch (Exception e) {
            logger.warn("debug.html の出力に失敗しました", e);
        }
    }

    private void generateIndex(List<HandleResult> results) {
        Metrics.timer("jig.document.time", "phase", "index").record(() -> {
            IndexAdapter indexAdapter = new IndexAdapter();
            indexAdapter.render(results, outputDirectory);
        });
    }

    private void generateAssets() {
        Path assetsPath = outputDirectory.resolve("assets");
        for (String fileName : ASSET_FILES) {
            JigDocumentWriter.copyResourceTo("templates/assets/" + fileName, assetsPath.resolve(fileName));
        }
    }

    public void close(Consumer<Path> pathConsumer) {
        pathConsumer.accept(outputDirectory);
    }
}
