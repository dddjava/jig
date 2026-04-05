package org.dddjava.jig.adapter;

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
import java.util.Map;
import java.util.function.Consumer;

public class JigDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentGenerator.class);

    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;
    private final Map<JigDocument, List<JigDocumentAdapter>> adaptersMap;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();

        var typeRelationsDataAdapter = new TypeRelationsDataAdapter(jigService);
        this.adaptersMap = Map.of(
                JigDocument.DomainModel, List.of(new DomainDataAdapter(jigService), typeRelationsDataAdapter),
                JigDocument.PackageRelation, List.of(new PackageDataAdapter(jigService), typeRelationsDataAdapter),
                JigDocument.Glossary, List.of(new GlossaryDataAdapter(jigService)),
                JigDocument.Insight, List.of(new InsightDataAdapter(jigService)),
                JigDocument.InboundInterface, List.of(new InboundDataAdapter(jigService)),
                JigDocument.OutboundInterface, List.of(new OutboundDataAdapter(jigService)),
                JigDocument.UsecaseModel, List.of(new UsecaseDataAdapter(jigService)),
                JigDocument.ListOutput, List.of(new ListOutputDataAdapter(jigService))
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
        writeDataFiles(jigRepository);
        return jigDocuments.stream()
                .map(this::generateDocument)
                .toList();
    }

    private void writeDataFiles(JigRepository jigRepository) {
        jigDocuments.stream()
                .flatMap(doc -> adaptersMap.getOrDefault(doc, List.of()).stream())
                // 同じDataAdapterは一度だけ出力する
                .distinct()
                .forEach(adapter -> {
                    String jsonText = adapter.buildJson(jigRepository);
                    JigDocumentWriter.writeData(outputDirectory, adapter.dataFileName(), adapter.variableName(), jsonText);
                });
    }

    private HandleResult generateDocument(JigDocument jigDocument) {
        try {
            long startTime = System.currentTimeMillis();
            Path htmlPath = JigDocumentWriter.writeHtml(jigDocument, outputDirectory);
            logger.info("[{}] completed: {} ms", jigDocument, System.currentTimeMillis() - startTime);
            return HandleResult.withOutput(jigDocument, List.of(htmlPath));
        } catch (Exception e) {
            // ドキュメント出力に失敗しても例外を伝播させない
            logger.warn("[{}] failed to write document.", jigDocument, e);
            return HandleResult.withException(jigDocument, e);
        }
    }

    private void generateDebugHtml() {
        JigDocumentWriter.copyResourceTo("templates/debug.html", outputDirectory.resolve("debug.html"));
    }

    private void generateIndex(List<HandleResult> results) {
        IndexAdapter indexAdapter = new IndexAdapter();
        indexAdapter.render(results, outputDirectory);
    }

    private void generateAssets() {
        Path assetsPath = outputDirectory.resolve("assets");
        for (String fileName : List.of(
                // 共通
                "favicon.ico",
                "index.js",
                "style.css",
                "jig-dom.js", "jig-glossary.js", "jig-mermaid.js",
                // typesは開発用なので不要
                // "types.js",

                // 各ドキュメント用
                // TODO 出力対象のドキュメントだけにする
                "domain.js",
                "glossary.js",
                "inbound.js",
                "insight.js",
                "list-output.js",
                "outbound.js",
                "package.js",
                "usecase.js"
        )) {
            JigDocumentWriter.copyResourceTo("templates/assets/" + fileName, assetsPath.resolve(fileName));
        }
    }

    public void close(Consumer<Path> pathConsumer) {
        pathConsumer.accept(outputDirectory);
    }
}
