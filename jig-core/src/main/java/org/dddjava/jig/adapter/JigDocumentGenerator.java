package org.dddjava.jig.adapter;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.adapter.datajs.DataAdapterResolver;
import org.dddjava.jig.adapter.datajs.DiagnosticsDataAdapter;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.git.GitRepositoryInfo;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.JigDocumentContext;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JigDocumentGenerator {

    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;
    private final DataAdapterResolver dataAdapterResolver;
    private final JigDocumentWriter writer;
    private final JigEventRepository jigEventRepository;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService, JigEventRepository jigEventRepository) {
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();
        this.dataAdapterResolver = new DataAdapterResolver(jigService);
        this.writer = new JigDocumentWriter(outputDirectory, jigDocumentContext.locale());
        this.jigEventRepository = jigEventRepository;
    }

    public JigResult generate(JigRepository jigRepository, GitRepositoryInfo gitRepositoryInfo) {
        writer.prepareOutputDirectory();

        var handleResults = generateDocuments(jigRepository);

        generateIndex(handleResults, gitRepositoryInfo);
        generateDebugHtml();
        generateSharedAssets();
        return new JigResultData(handleResults, IndexAdapter.indexFilePath(outputDirectory));
    }

    private List<HandleResult> generateDocuments(JigRepository jigRepository) {
        writeDataFiles(jigRepository);
        // ドキュメント生成中に記録される警告も含めるため、他のデータを出力し終えてから書く
        writeDiagnosticsData(jigRepository);
        return jigDocuments.stream()
                .map(jigDocument -> {
                    Path htmlPath = writer.writeHtmlAndJs(jigDocument);
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
                    writer.writeData(adapter.dataFileName(), adapter.variableName(), jsonText);
                });
    }

    /**
     * 解析中に検出した事象を出力する。ソース読み取り時のものはJigRepositoryが、
     * ドキュメント生成時のものはJigEventRepositoryが持っている。
     */
    private void writeDiagnosticsData(JigRepository jigRepository) {
        var diagnostics = Stream.concat(jigRepository.diagnostics().stream(), jigEventRepository.diagnostics().stream())
                .distinct()
                .toList();
        writer.writeData(DiagnosticsDataAdapter.dataFileName(), DiagnosticsDataAdapter.variableName(),
                DiagnosticsDataAdapter.buildJson(diagnostics));
    }

    private void generateIndex(List<HandleResult> results, GitRepositoryInfo gitRepositoryInfo) {
        new IndexAdapter(writer).render(results, gitRepositoryInfo);
    }

    private void generateDebugHtml() {
        writer.copyResourceTo("templates/", "debug.html", outputDirectory);
    }

    void generateSharedAssets() {
        for (String fileName : List.of(
                "favicon.ico",
                "index.js",
                "common.css",
                "package-relation.css",
                "outbound-interface.css",
                "list-output.css",
                "glossary.css",
                "index.css",
                "library-dependency.css",
                "usecase.css",
                "insight.css",
                "jig-bundle.js"

                // 開発用なので不要
                // "types.js"
        )) {
            writer.copyAssetsResource(fileName);
        }
    }

    public void close(Consumer<Path> pathConsumer) {
        pathConsumer.accept(outputDirectory);
    }
}
