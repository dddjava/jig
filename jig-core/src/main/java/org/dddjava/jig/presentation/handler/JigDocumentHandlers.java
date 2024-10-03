package org.dddjava.jig.presentation.handler;

import org.dddjava.jig.application.service.JigService;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.ClassRelationDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.models.applications.backends.DatasourceAngles;
import org.dddjava.jig.domain.model.models.applications.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.models.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.services.StringComparingMethodList;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackages;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.domains.collections.JigCollectionTypes;
import org.dddjava.jig.domain.model.models.domains.validations.Validations;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.presentation.view.graphviz.dot.DotView;
import org.dddjava.jig.presentation.view.html.IndexView;
import org.dddjava.jig.presentation.view.html.JigExpressionObjectDialect;
import org.dddjava.jig.presentation.view.html.SummaryView;
import org.dddjava.jig.presentation.view.html.TableView;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.business_rule.*;
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

    private final JigDocumentContext jigDocumentContext;
    private final JigDiagramFormat diagramFormat;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    private final DotCommandRunner dotCommandRunner;
    private final TemplateEngine templateEngine;
    private final JigService jigService;

    private JigDocumentHandlers(JigDocumentContext jigDocumentContext,
                                JigDiagramFormat diagramFormat,
                                JigService jigService,
                                List<JigDocument> jigDocuments,
                                Path outputDirectory) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
        this.diagramFormat = diagramFormat;
        this.jigDocuments = jigDocuments;
        this.outputDirectory = outputDirectory;

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

    public static JigDocumentHandlers from(JigDocumentContext jigDocumentContext, JigService jigService, JigDiagramFormat outputDiagramFormat, List<JigDocument> jigDocuments, Path outputDirectory) {
        return new JigDocumentHandlers(
                jigDocumentContext,
                outputDiagramFormat,
                jigService,
                jigDocuments,
                outputDirectory
        );
    }

    public HandleResults handleJigDocuments() {
        long startTime = System.currentTimeMillis();
        logger.info("[JIG] write jig documents: {}", jigDocuments);

        prepareOutputDirectory();

        var writtenResults = writeJigDocuments();

        writeIndexHtml(outputDirectory, writtenResults);

        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        return new HandleResults(writtenResults);
    }

    private List<HandleResult> writeJigDocuments() {
        return jigDocuments
                .parallelStream()
                .map(jigDocument -> handle(jigDocument, outputDirectory))
                .collect(Collectors.toList());
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

    private HandleResult handle(JigDocument jigDocument, Path outputDirectory) {
        try {
            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);

            long startTime = System.currentTimeMillis();

            Object model = switch (jigDocument) {
                case BusinessRuleList -> domainList();
                case PackageRelationDiagram -> jigService.packageDependencies();
                case BusinessRuleRelationDiagram -> new ClassRelationDiagram(jigService.businessRules());
                case CategoryDiagram -> jigService.categories();
                case CategoryUsageDiagram -> jigService.categoryUsages();
                case ApplicationList -> applicationList();
                case ServiceMethodCallHierarchyDiagram -> jigService.serviceMethodCallHierarchy();
                case CompositeUsecaseDiagram -> new CompositeUsecaseDiagram(jigService.serviceAngles());
                case ArchitectureDiagram -> jigService.architectureDiagram();
                case DomainSummary -> SummaryModel.from(jigService.businessRules());
                case ApplicationSummary -> SummaryModel.from(jigService.serviceMethods());
                case UsecaseSummary -> usecaseSummary();
                case EntrypointSummary -> entrypointSummary();
                case EnumSummary ->
                        SummaryModel.from(jigService.categoryTypes(), jigService.enumModels());
                case TermTable -> jigService.terms();
                case TermList ->
                        new ModelReports(new ModelReport<>(jigService.terms().list(), TermReport::new, TermReport.class));
            };

            JigView jigView = switch (jigDocument.jigDocumentType()) {
                case LIST -> new ModelReportsPoiView(jigDocumentContext);
                case DIAGRAM -> new DotView(diagramFormat, dotCommandRunner, jigDocumentContext);
                case SUMMARY -> new SummaryView(templateEngine, jigDocumentContext);
                case TABLE -> new TableView(templateEngine);
            };
            jigView.render(model, jigDocumentWriter);

            long takenTime = System.currentTimeMillis() - startTime;
            logger.info("[{}] completed: {} ms", jigDocument, takenTime);
            return new HandleResult(jigDocument, jigDocumentWriter.outputFilePaths());
        } catch (Exception e) {
            logger.warn("[{}] failed to write document.", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }

    private void writeIndexHtml(Path outputDirectory, List<HandleResult> handleResultList) {
        IndexView indexView = new IndexView(templateEngine, diagramFormat);
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

    private ModelReports domainList() {
        MethodSmellList angles = jigService.methodSmells();
        JigTypes jigTypes = jigService.jigTypes();

        JigCollectionTypes jigCollectionTypes = jigService.collections();
        CategoryDiagram categoryDiagram = jigService.categories();
        BusinessRules businessRules = jigService.businessRules();
        BusinessRulePackages businessRulePackages = jigService.businessRules().businessRulePackages();
        return new ModelReports(
                new ModelReport<>(businessRulePackages.list(), PackageReport::new, PackageReport.class),
                new ModelReport<>(businessRules.list(),
                        businessRule -> new BusinessRuleReport(businessRule, businessRules),
                        BusinessRuleReport.class),
                new ModelReport<>(categoryDiagram.list(), CategoryReport::new, CategoryReport.class),
                new ModelReport<>(jigCollectionTypes.listJigType(),
                        jigType -> new CollectionReport(jigType, jigCollectionTypes.classRelations()),
                        CollectionReport.class),
                new ModelReport<>(Validations.from(jigTypes).list(), ValidationReport::new, ValidationReport.class),
                new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class)
        );
    }

    private ModelReports applicationList() {
        ServiceAngles serviceAngles = jigService.serviceAngles();

        DatasourceAngles datasourceAngles = jigService.datasourceAngles();
        StringComparingMethodList stringComparingMethodList = jigService.stringComparing();
        HandlerMethods handlerMethods = jigService.controllerAngles();

        return new ModelReports(
                new ModelReport<>(handlerMethods.list(),
                        requestHandlerMethod -> new ControllerReport(requestHandlerMethod),
                        ControllerReport.class),
                new ModelReport<>(serviceAngles.list(),
                        serviceAngle -> new ServiceReport(serviceAngle),
                        ServiceReport.class),
                new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class),
                new ModelReport<>(stringComparingMethodList.list(), StringComparingReport::new, StringComparingReport.class)
        );
    }

    private SummaryModel usecaseSummary() {
        ServiceAngles serviceAngles = jigService.serviceAngles();

        record Entry(JigMethod jigMethod, String mermaidText) {
        }

        var mermaidMap = serviceAngles.list()
                .stream()
                // どのServiceMethodにも使用されていないものだけを起点にする
                //.filter(serviceAngle -> serviceAngle.userServiceMethods().empty())
                // どのServiceMethodも使用していないものは除外する
                //.filter(serviceAngle -> !serviceAngle.usingServiceMethods().empty())
                .map(rootServiceMethod -> {
                    var key = rootServiceMethod.method().asSimpleText();

                    return new Entry(
                            rootServiceMethod.serviceMethod().method(),
                            serviceAngles.mermaidText(key)
                    );
                })
                .collect(Collectors.groupingBy(
                        entry -> entry.jigMethod.fqn(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream().findFirst().map(entry -> entry.mermaidText()).orElse(null))
                ));

        return SummaryModel.from(jigService.serviceMethods(), mermaidMap);
    }

    private SummaryModel entrypointSummary() {
        return SummaryModel.from(jigService.entrypoint());
    }
}
