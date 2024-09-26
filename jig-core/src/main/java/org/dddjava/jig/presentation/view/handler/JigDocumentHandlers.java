package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.presentation.controller.JigController;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.presentation.view.graphviz.dot.DotView;
import org.dddjava.jig.presentation.view.html.IndexView;
import org.dddjava.jig.presentation.view.html.JigExpressionObjectDialect;
import org.dddjava.jig.presentation.view.html.SummaryView;
import org.dddjava.jig.presentation.view.html.TableView;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
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

public class JigDocumentHandlers {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentHandlers.class);

    private final ViewResolver viewResolver;
    private final JigController jigController;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    public JigDocumentHandlers(ViewResolver viewResolver,
                               JigController jigController,
                               List<JigDocument> jigDocuments,
                               Path outputDirectory) {
        this.viewResolver = viewResolver;
        this.jigController = jigController;
        this.jigDocuments = jigDocuments;
        this.outputDirectory = outputDirectory;
    }

    public static JigDocumentHandlers from(JigDocumentContext jigDocumentContext, DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService, JigDiagramFormat outputDiagramFormat, List<JigDocument> jigDocuments, Path outputDirectory) {
        return new JigDocumentHandlers(
                new ViewResolver(
                        outputDiagramFormat,
                        jigDocumentContext
                ),
                new JigController(dependencyService, businessRuleService, applicationService),
                jigDocuments,
                outputDirectory
        );
    }

    public HandleResults handleJigDocuments() {
        long startTime = System.currentTimeMillis();
        logger.info("[JIG] write jig documents: {}", jigDocuments);

        prepareOutputDirectory();

        List<HandleResult> handleResultList = jigDocuments
                .parallelStream()
                .map(jigDocument -> handle(jigDocument, outputDirectory))
                .collect(Collectors.toList());
        writeIndexHtml(outputDirectory, handleResultList);
        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        return new HandleResults(handleResultList);
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

    HandleResult handle(JigDocument jigDocument, Path outputDirectory) {
        try {
            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);

            long startTime = System.currentTimeMillis();
            Object model = jigController.handle(jigDocument);

            JigView jigView = viewResolver.resolve(jigDocument);
            jigView.render(model, jigDocumentWriter);

            long takenTime = System.currentTimeMillis() - startTime;
            logger.info("[{}] completed: {} ms", jigDocument, takenTime);
            return new HandleResult(jigDocument, jigDocumentWriter.outputFilePaths());
        } catch (Exception e) {
            logger.warn("[{}] failed to write document.", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }

    void writeIndexHtml(Path outputDirectory, List<HandleResult> handleResultList) {
        IndexView indexView = viewResolver.indexView();
        indexView.render(handleResultList, outputDirectory);
        copyAssets(outputDirectory);
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

    static class ViewResolver {

        JigDiagramFormat diagramFormat;

        JigDocumentContext jigDocumentContext;
        DotCommandRunner dotCommandRunner;
        TemplateEngine templateEngine;

        public ViewResolver(JigDiagramFormat diagramFormat, JigDocumentContext jigDocumentContext) {
            this.jigDocumentContext = jigDocumentContext;
            this.diagramFormat = diagramFormat;

            // setup Graphviz
            this.dotCommandRunner = new DotCommandRunner();

            // setup Thymeleaf
            TemplateEngine templateEngine = new TemplateEngine();
            ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateResolver.setSuffix(".html");
            templateResolver.setPrefix("templates/");
            templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
            templateEngine.setTemplateResolver(templateResolver);
            templateEngine.addDialect(new JigExpressionObjectDialect(jigDocumentContext));
            this.templateEngine = templateEngine;
        }

        JigView resolve(JigDocument jigDocument) {
            switch (jigDocument.jigDocumentType()) {
                case LIST:
                    return new ModelReportsPoiView(jigDocumentContext);
                case DIAGRAM:
                    return new DotView(diagramFormat, dotCommandRunner, jigDocumentContext);
                case SUMMARY:
                    return new SummaryView(templateEngine, jigDocumentContext);
                case TABLE:
                    return new TableView(templateEngine, jigDocumentContext);
            }

            throw new IllegalArgumentException("View未定義のJigDocumentを出力しようとしています: " + jigDocument);
        }

        IndexView indexView() {
            return new IndexView(templateEngine, diagramFormat);
        }
    }
}
