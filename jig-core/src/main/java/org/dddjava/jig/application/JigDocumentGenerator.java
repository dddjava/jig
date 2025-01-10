package org.dddjava.jig.application;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.ListAdapter;
import org.dddjava.jig.adapter.SummaryAdapter;
import org.dddjava.jig.adapter.diagram.DiagramAdapter;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.infrastructure.view.graphviz.dot.GraphvizDiagramWriter;
import org.dddjava.jig.infrastructure.view.html.IndexView;
import org.dddjava.jig.infrastructure.view.html.JigExpressionObjectDialect;
import org.dddjava.jig.infrastructure.view.html.TableView;
import org.dddjava.jig.infrastructure.view.html.ThymeleafSummaryWriter;
import org.dddjava.jig.infrastructure.view.poi.report.ReportBook;
import org.dddjava.jig.infrastructure.view.poi.report.ReportSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class JigDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentGenerator.class);

    private final JigDiagramFormat diagramFormat;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    private final TemplateEngine thymeleafTemplateEngine;
    private final JigService jigService;

    private final DiagramAdapter diagramAdapter;
    private final ListAdapter listAdapter;
    private final SummaryAdapter summaryAdapter;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigService = jigService;
        this.diagramFormat = jigDocumentContext.diagramFormat();
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
        templateEngine.addDialect(new JigExpressionObjectDialect(jigDocumentContext));
        this.thymeleafTemplateEngine = templateEngine;

        diagramAdapter = new DiagramAdapter(jigService, new GraphvizDiagramWriter(jigDocumentContext));
        listAdapter = new ListAdapter(jigDocumentContext, jigService);
        summaryAdapter = new SummaryAdapter(jigService, new ThymeleafSummaryWriter(templateEngine, jigDocumentContext));
    }

    public void generateIndex(List<HandleResult> results) {
        IndexView indexView = new IndexView(thymeleafTemplateEngine, diagramFormat);
        indexView.render(results, outputDirectory);
        copyAssets(outputDirectory);
    }

    public List<HandleResult> generateDocuments(JigSource jigSource) {
        return jigDocuments
                .parallelStream()
                .map(jigDocument -> generateDocument(jigDocument, outputDirectory, jigSource))
                .collect(Collectors.toList());
    }

    public void prepareOutputDirectory() {
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

    HandleResult generateDocument(JigDocument jigDocument, Path outputDirectory, JigSource jigSource) {
        try {
            long startTime = System.currentTimeMillis();

            var outputFilePaths = switch (jigDocument) {
                // 概要
                case DomainSummary, ApplicationSummary, UsecaseSummary, EntrypointSummary, EnumSummary ->
                        invokeAdapter(summaryAdapter, jigDocument, jigSource);
                // テーブル
                case TermTable -> {
                    var terms = jigService.terms(jigSource);
                    yield new TableView(jigDocument, thymeleafTemplateEngine).write(outputDirectory, terms);
                }
                // ダイアグラム
                case PackageRelationDiagram, BusinessRuleRelationDiagram, CategoryDiagram, CategoryUsageDiagram,
                     ServiceMethodCallHierarchyDiagram, CompositeUsecaseDiagram, ArchitectureDiagram ->
                        invokeAdapter(diagramAdapter, jigDocument, jigSource);
                // 一覧
                case TermList -> {
                    Terms terms = jigService.terms(jigSource);
                    var modelReports = new ReportBook(new ReportSheet<>("TERM", Terms.reporter(), terms.list()));
                    yield modelReports.writeXlsx(jigDocument, outputDirectory);
                }
                case BusinessRuleList, ApplicationList -> invokeAdapter(listAdapter, jigDocument, jigSource);
            };

            long takenTime = System.currentTimeMillis() - startTime;
            logger.info("[{}] completed: {} ms", jigDocument, takenTime);
            return new HandleResult(jigDocument, outputFilePaths);
        } catch (Exception e) {
            logger.warn("[{}] failed to write document.", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }

    private <T> List<Path> invokeAdapter(Adapter<T> adapter, JigDocument jigDocument, JigSource jigSource) {
        List<Method> invokeTargetMethod = Arrays.stream(adapter.getClass().getMethods())
                .filter(method -> Optional.ofNullable(method.getAnnotation(HandleDocument.class))
                        .map(HandleDocument::value)
                        .filter(values -> Arrays.asList(values).contains(jigDocument))
                        .isPresent())
                .toList();
        if (invokeTargetMethod.isEmpty()) {
            logger.error("{} に対応するハンドラが {} に見つかりませんでした。ドキュメントは生成されません。他のバージョンを使用するか、Issueで報告してください。",
                    jigDocument, adapter.getClass().getName());
            return List.of();
        }
        if (invokeTargetMethod.size() > 1) {
            logger.error("{} に対応するハンドラが {} に複数見つかりました。ドキュメントは生成されますが、意図したものにならない可能性があります。他のバージョンを使用するか、Issueで報告してください。",
                    jigDocument, adapter.getClass().getName());
        }

        return invokeTargetMethod.stream().findFirst()
                .map(method -> {
                    try {
                        return adapter.convertMethodResultToAdapterModel(method.invoke(adapter, jigSource));
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(result -> adapter.write(result, jigDocument))
                .orElseThrow(() -> new UnsupportedOperationException("no adapter method found for %s".formatted(jigDocument)));
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

