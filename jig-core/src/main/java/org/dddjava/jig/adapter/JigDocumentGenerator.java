package org.dddjava.jig.adapter;

import io.micrometer.core.instrument.Metrics;
import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.adapter.graphviz.DiagramAdapter;
import org.dddjava.jig.adapter.graphviz.GraphvizDiagramWriter;
import org.dddjava.jig.adapter.poi.ListAdapter;
import org.dddjava.jig.adapter.thymeleaf.*;
import org.dddjava.jig.adapter.thymeleaf.dialect.JigDialect;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDiagramOption;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class JigDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentGenerator.class);

    private final JigDiagramOption diagramOption;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    private final TemplateEngine thymeleafTemplateEngine;
    private final JigService jigService;

    private final CompositeAdapter compositeAdapter;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigService = jigService;
        this.diagramOption = jigDocumentContext.diagramOption();
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();

        // setup Thymeleaf
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(new JigDialect(jigDocumentContext));
        this.thymeleafTemplateEngine = templateEngine;

        compositeAdapter = new CompositeAdapter();
        compositeAdapter.register(new DiagramAdapter(jigService, new GraphvizDiagramWriter(jigDocumentContext)));
        compositeAdapter.register(new ListAdapter(jigDocumentContext, jigService));
        compositeAdapter.register(new SummaryAdapter(jigService, new ThymeleafSummaryWriter(templateEngine, jigDocumentContext)));
        compositeAdapter.register(new InsightAdapter(jigService, templateEngine, jigDocumentContext));
        compositeAdapter.register(new OutputsSummaryAdapter(jigService, templateEngine, jigDocumentContext));
        compositeAdapter.register(new ListOutputAdapter(jigService, templateEngine, jigDocumentContext));
    }

    public JigResult generate(JigRepository jigRepository) {
        prepareOutputDirectory();

        var handleResults = generateDocuments(jigRepository);

        generateIndex(handleResults);
        generateAssets();
        return new JigResultData(handleResults, IndexView.indexFilePath(outputDirectory));
    }

    private void prepareOutputDirectory() {
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
                .map(jigDocument -> generateDocument(jigDocument, outputDirectory, jigRepository))
                .toList();
    }

    // テストのために可視性を緩めている
    HandleResult generateDocument(JigDocument jigDocument, Path outputDirectory, JigRepository jigRepository) {
        return Objects.requireNonNull(Metrics.timer("jig.document.time", "phase", jigDocument.name()).record(() -> {
            try {
                long startTime = System.currentTimeMillis();

                var outputFilePaths = switch (jigDocument) {
                    case Glossary -> new TableView(jigDocument, thymeleafTemplateEngine)
                            .write(outputDirectory, jigService.glossary(jigRepository));
                    case PackageSummary -> new PackageSummaryView(jigDocument, thymeleafTemplateEngine)
                            .write(
                                    outputDirectory,
                                    jigService.packages(jigRepository),
                                    jigService.packageRelations(jigRepository),
                                    jigService.typeRelationships(jigRepository)
                            );
                    case DomainSummary, ApplicationSummary, UsecaseSummary, EntrypointSummary,
                         PackageRelationDiagram, BusinessRuleRelationDiagram, CategoryDiagram, CategoryUsageDiagram,
                         ServiceMethodCallHierarchyDiagram,
                         BusinessRuleList, ApplicationList, ListOutput,
                         OutputsSummary, Insight, Sequence -> compositeAdapter.invoke(jigDocument, jigRepository);
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

    private void generateIndex(List<HandleResult> results) {
        Metrics.timer("jig.document.time", "phase", "index").record(() -> {
            IndexView indexView = new IndexView(thymeleafTemplateEngine, diagramOption.graphvizOutputFormat());
            indexView.render(results, outputDirectory);
        });
    }

    private void generateAssets() {
        try {
            Path assetsPath = this.outputDirectory.resolve("assets");
            Files.createDirectories(assetsPath);
            copyAsset("style.css", assetsPath);
            copyAsset("jig.js", assetsPath);
            copyAsset("favicon.ico", assetsPath);
            // ページごとのスクリプトを追加する
            //   増えるごとにここに追加しなきゃいけないのはいかがなものか
            copyAsset("package.js", assetsPath);
            copyAsset("glossary.js", assetsPath);
            copyAsset("insight.js", assetsPath);
            copyAsset("list-output.js", assetsPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void copyAsset(String fileName, Path distDirectory) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("templates/assets/" + fileName)) {
            Files.copy(Objects.requireNonNull(is), distDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void close(Consumer<Path> pathConsumer) {
        pathConsumer.accept(outputDirectory);
    }
}
