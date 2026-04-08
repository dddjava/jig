package org.dddjava.jig.adapter;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.adapter.datajs.DataAdapterResolver;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class JigDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentGenerator.class);

    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;
    private final DataAdapterResolver dataAdapterResolver;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();
        this.dataAdapterResolver = new DataAdapterResolver(jigService);
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
                .map(jigDocument -> {
                    Path htmlPath = JigDocumentWriter.writeHtml(jigDocument, outputDirectory);
                    return HandleResult.withOutput(jigDocument, List.of(htmlPath));
                })
                .toList();
    }

    private void writeDataFiles(JigRepository jigRepository) {
        jigDocuments.stream()
                .flatMap(doc -> dataAdapterResolver.resolve(doc).stream())
                // 同じDataAdapterは一度だけ出力する
                .distinct()
                .forEach(adapter -> {
                    String jsonText = adapter.buildJson(jigRepository);
                    JigDocumentWriter.writeData(outputDirectory, adapter.dataFileName(), adapter.variableName(), jsonText);
                });
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
                "jig-bundle.js",
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
