package org.dddjava.jig.adapter;

import io.micrometer.core.instrument.Metrics;
import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.adapter.graphviz.DiagramAdapter;
import org.dddjava.jig.adapter.graphviz.GraphvizDiagramWriter;
import org.dddjava.jig.adapter.html.*;
import org.dddjava.jig.adapter.poi.ListAdapter;

import org.dddjava.jig.adapter.html.view.*;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDiagramOption;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class JigDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentGenerator.class);

    private final JigDiagramOption diagramOption;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    private final CompositeAdapter compositeAdapter;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.diagramOption = jigDocumentContext.diagramOption();
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();

        compositeAdapter = new CompositeAdapter();
        compositeAdapter.register(new DiagramAdapter(jigService, new GraphvizDiagramWriter(jigDocumentContext)));
        compositeAdapter.register(new ListAdapter(jigDocumentContext, jigService));
        compositeAdapter.register(new DomainSummaryAdapter(jigService, jigDocumentContext));
        compositeAdapter.register(new InsightAdapter(jigService, jigDocumentContext));
        compositeAdapter.register(new OutputsSummaryAdapter(jigService, jigDocumentContext));
        compositeAdapter.register(new InboundSummaryAdapter(jigService, jigDocumentContext));
        compositeAdapter.register(new UsecaseSummaryAdapter(jigService, jigDocumentContext));
        compositeAdapter.register(new ListOutputAdapter(jigService, jigDocumentContext));
        compositeAdapter.register(new GlossaryAdapter(jigService, jigDocumentContext));
        compositeAdapter.register(new PackageSummaryAdapter(jigService, jigDocumentContext));
    }

    public JigResult generate(JigRepository jigRepository) {
        prepareOutputDirectory();

        var handleResults = generateDocuments(jigRepository);

        generateIndex(handleResults);
        generateDebugHtml();
        generateAssets();
        return new JigResultData(handleResults, IndexView.indexFilePath(outputDirectory));
    }

    private void prepareOutputDirectory() {
        createOutputDirectory(outputDirectory);
        createOutputDirectory(outputDirectory.resolve("assets"));
        createOutputDirectory(outputDirectory.resolve("data"));
    }

    private void createOutputDirectory(Path outputDirectory) {
        File file = outputDirectory.toFile();
        if (file.exists()) {
            if (file.isDirectory() && file.canWrite()) {
                // ディレクトリかつ書き込み可能なので対応不要
                return;
            }
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " is not Directory. Please review your settings.");
            }
            if (file.isDirectory() && !file.canWrite()) {
                throw new IllegalStateException(file.getAbsolutePath() + " can not writable. Please specify another directory.");
            }
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

                var outputFilePaths = switch (jigDocument) {
                    case DomainSummary, UsecaseSummary, EntrypointSummary,
                         PackageRelationDiagram, BusinessRuleRelationDiagram, CategoryDiagram, CategoryUsageDiagram,
                         ServiceMethodCallHierarchyDiagram,
                         BusinessRuleList, ApplicationList, ListOutput,
                         OutputsSummary, Insight, Glossary, PackageSummary -> compositeAdapter.invoke(jigDocument, jigRepository);
                };

                long takenTime = System.currentTimeMillis() - startTime;
                logger.info("[{}] completed: {} ms", jigDocument, takenTime);
                return HandleResult.withOutput(jigDocument, outputFilePaths);
            } catch (Exception e) {
                // ドキュメント出力に失敗しても例外を伝播させない
                logger.warn("[{}] failed to write document.", jigDocument, e);
                return HandleResult.withException(jigDocument, e);
            }
        }));
    }

    private void generateDebugHtml() {
        Path outputPath = outputDirectory.resolve("debug.html");
        try (var resource = JigDocumentGenerator.class.getResourceAsStream("/templates/debug.html")) {
            if (resource != null) {
                Files.copy(resource, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.warn("debug.html の出力に失敗しました", e);
        }
    }

    private void generateIndex(List<HandleResult> results) {
        Metrics.timer("jig.document.time", "phase", "index").record(() -> {
            IndexView indexView = new IndexView(diagramOption.graphvizOutputFormat());
            indexView.render(results, outputDirectory);
        });
    }

    private void generateAssets() {
        try {
            Path assetsPath = this.outputDirectory.resolve("assets");
            Files.createDirectories(assetsPath);
            for (String assetRelativePath : listAssetRelativePaths()) {
                copyAsset(assetRelativePath, assetsPath);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> listAssetRelativePaths() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("templates/assets");
        if (resource == null) {
            throw new IOException("templates/assets が見つかりませんでした。");
        }
        try {
            URI uri = resource.toURI();
            if ("jar".equalsIgnoreCase(uri.getScheme())) {
                return listAssetRelativePathsFromJar(uri);
            }
            return listAssetRelativePathsFromDirectory(Paths.get(uri));
        } catch (URISyntaxException e) {
            throw new IOException("templates/assets のURI解決に失敗しました。", e);
        }
    }

    private List<String> listAssetRelativePathsFromJar(URI jarUri) throws IOException {
        try (FileSystem fileSystem = openJarFileSystem(jarUri)) {
            return listAssetRelativePathsFromDirectory(fileSystem.getPath("/templates/assets"));
        }
    }

    private FileSystem openJarFileSystem(URI jarUri) throws IOException {
        try {
            return FileSystems.newFileSystem(jarUri, Map.of());
        } catch (FileSystemAlreadyExistsException ignored) {
            return FileSystems.getFileSystem(jarUri);
        }
    }

    private List<String> listAssetRelativePathsFromDirectory(Path assetsRoot) throws IOException {
        try (var paths = Files.walk(assetsRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(assetsRoot::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .sorted()
                    .toList();
        }
    }

    private void copyAsset(String fileName, Path distDirectory) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("templates/assets/" + fileName)) {
            if (is == null) {
                throw new IOException("assets が見つかりませんでした: " + fileName);
            }
            Path outputPath = distDirectory.resolve(fileName);
            Files.createDirectories(outputPath.getParent());
            Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void close(Consumer<Path> pathConsumer) {
        pathConsumer.accept(outputDirectory);
    }
}
