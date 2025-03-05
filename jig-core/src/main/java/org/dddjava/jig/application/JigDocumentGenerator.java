package org.dddjava.jig.application;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.adapter.CompositeAdapter;
import org.dddjava.jig.adapter.diagram.DiagramAdapter;
import org.dddjava.jig.adapter.diagram.GraphvizDiagramWriter;
import org.dddjava.jig.adapter.excel.GlossaryAdapter;
import org.dddjava.jig.adapter.excel.ListAdapter;
import org.dddjava.jig.adapter.html.IndexView;
import org.dddjava.jig.adapter.html.SummaryAdapter;
import org.dddjava.jig.adapter.html.TableView;
import org.dddjava.jig.adapter.html.ThymeleafSummaryWriter;
import org.dddjava.jig.adapter.html.dialect.JigDialect;
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
import java.util.stream.Collectors;

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
    }

    public void generateIndex(List<HandleResult> results) {
        IndexView indexView = new IndexView(thymeleafTemplateEngine, diagramOption.graphvizOutputFormat());
        indexView.render(results, outputDirectory);
        copyAssets(outputDirectory);
    }

    public List<HandleResult> generateDocuments(JigRepository jigRepository) {
        prepareOutputDirectory();
        List<HandleResult> handleResults = jigDocuments
                .parallelStream()
                .map(jigDocument -> generateDocument(jigDocument, outputDirectory, jigRepository))
                .collect(Collectors.toList());
        jigService.notifyReportInformation();
        return handleResults;
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

    HandleResult generateDocument(JigDocument jigDocument, Path outputDirectory, JigRepository jigRepository) {
        try {
            long startTime = System.currentTimeMillis();

            var outputFilePaths = switch (jigDocument) {
                case TermTable -> new TableView(jigDocument, thymeleafTemplateEngine)
                        .write(outputDirectory, jigService.glossary(jigRepository));
                case TermList ->
                        GlossaryAdapter.invoke(jigService.glossary(jigRepository), jigDocument, outputDirectory);
                case DomainSummary, ApplicationSummary, UsecaseSummary, EntrypointSummary, EnumSummary,
                     PackageRelationDiagram, BusinessRuleRelationDiagram, CategoryDiagram, CategoryUsageDiagram,
                     ServiceMethodCallHierarchyDiagram, CompositeUsecaseDiagram, ArchitectureDiagram,
                     BusinessRuleList, ApplicationList -> compositeAdapter.invoke(jigDocument, jigRepository);
            };

            long takenTime = System.currentTimeMillis() - startTime;
            logger.info("[{}] completed: {} ms", jigDocument, takenTime);
            return new HandleResult(jigDocument, outputFilePaths);
        } catch (Exception e) {
            logger.warn("[{}] failed to write document.", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }

    private void copyAssets(Path outputDirectory) {
        Path assetsDirectory = createAssetsDirectory(outputDirectory);
        copyAsset("style.css", assetsDirectory);
        copyAsset("jig.js", assetsDirectory);
        copyAsset("favicon.ico", assetsDirectory);
    }

    private static Path createAssetsDirectory(Path outputDirectory) {
        Path assetsPath = outputDirectory.resolve("assets");
        try {
            Files.createDirectories(assetsPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return assetsPath;
    }

    private void copyAsset(String fileName, Path distDirectory) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("templates/assets/" + fileName)) {
            Files.copy(Objects.requireNonNull(is), distDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

