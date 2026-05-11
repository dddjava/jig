package org.dddjava.jig.adapter;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.adapter.datajs.DataAdapterResolver;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.JigDocumentContext;
import org.dddjava.jig.infrastructure.git.GitRepositoryInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class JigDocumentGenerator {

    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;
    private final DataAdapterResolver dataAdapterResolver;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();
        this.dataAdapterResolver = new DataAdapterResolver(jigService);
    }

    public JigResult generate(JigRepository jigRepository, GitRepositoryInfo gitRepositoryInfo) {
        JigDocumentWriter.prepareOutputDirectory(outputDirectory);

        var handleResults = generateDocuments(jigRepository);

        generateIndex(handleResults, gitRepositoryInfo);
        generateDebugHtml();
        generateSharedAssets();
        return new JigResultData(handleResults, IndexAdapter.indexFilePath(outputDirectory));
    }

    private List<HandleResult> generateDocuments(JigRepository jigRepository) {
        writeDataFiles(jigRepository);
        return jigDocuments.stream()
                .map(jigDocument -> {
                    Path htmlPath = JigDocumentWriter.writeHtmlAndJs(jigDocument, outputDirectory);
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

    private void generateIndex(List<HandleResult> results, GitRepositoryInfo gitRepositoryInfo) {
        IndexAdapter indexAdapter = new IndexAdapter();
        indexAdapter.render(results, outputDirectory, gitRepositoryInfo);
    }

    private void generateDebugHtml() {
        JigDocumentWriter.copyResourceTo("templates/", "debug.html", outputDirectory);
    }

    void generateSharedAssets() {
        for (String fileName : List.of(
                "favicon.ico",
                "index.js",
                "style.css",
                "jig-bundle.js"

                // 開発用なので不要
                // "types.js"
        )) {
            JigDocumentWriter.copyAssetsResource(fileName, outputDirectory);
        }
    }

    public void close(Consumer<Path> pathConsumer) {
        pathConsumer.accept(outputDirectory);
    }
}
