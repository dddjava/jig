package org.dddjava.jig.application;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.ClassRelationDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.models.applications.backends.DatasourceAngles;
import org.dddjava.jig.domain.model.models.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.services.StringComparingMethodList;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.domains.validations.Validations;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.infrastructure.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.infrastructure.view.graphviz.dot.DotView;
import org.dddjava.jig.infrastructure.view.html.IndexView;
import org.dddjava.jig.infrastructure.view.html.JigExpressionObjectDialect;
import org.dddjava.jig.infrastructure.view.html.SummaryView;
import org.dddjava.jig.infrastructure.view.html.TableView;
import org.dddjava.jig.infrastructure.view.poi.ModelReportsPoiView;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReport;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReports;
import org.dddjava.jig.infrastructure.view.report.application.ControllerReport;
import org.dddjava.jig.infrastructure.view.report.application.RepositoryReport;
import org.dddjava.jig.infrastructure.view.report.application.ServiceReport;
import org.dddjava.jig.infrastructure.view.report.business_rule.*;
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

    private final JigDocumentContext jigDocumentContext;
    private final JigDiagramFormat diagramFormat;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    private final DotCommandRunner dotCommandRunner;
    private final TemplateEngine templateEngine;
    private final JigService jigService;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
        this.diagramFormat = jigDocumentContext.diagramFormat();
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();

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

    public static JigDocumentGenerator from(JigDocumentContext jigDocumentContext, JigService jigService) {
        return new JigDocumentGenerator(jigDocumentContext, jigService);
    }

    public List<HandleResult> generate(JigSource jigSource) {
        long startTime = System.currentTimeMillis();
        logger.info("[JIG] write jig documents: {}", jigDocuments);

        prepareOutputDirectory();

        var writtenResults = jigDocuments
                .parallelStream()
                .map(jigDocument -> handle(jigDocument, outputDirectory, jigSource))
                .collect(Collectors.toList());

        writeIndexHtml(outputDirectory, writtenResults);

        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        return writtenResults;
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

    private HandleResult handle(JigDocument jigDocument, Path outputDirectory, JigSource jigSource) {
        try {
            long startTime = System.currentTimeMillis();

            Object model = switch (jigDocument) {
                case BusinessRuleList -> domainList(jigSource);
                case PackageRelationDiagram -> jigService.packageDependencies(jigSource);
                case BusinessRuleRelationDiagram -> new ClassRelationDiagram(jigService.businessRules(jigSource));
                case CategoryDiagram -> jigService.categories(jigSource);
                case CategoryUsageDiagram -> jigService.categoryUsages(jigSource);
                case ApplicationList -> applicationList(jigSource);
                case ServiceMethodCallHierarchyDiagram -> jigService.serviceMethodCallHierarchy(jigSource);
                case CompositeUsecaseDiagram -> new CompositeUsecaseDiagram(jigService.serviceAngles(jigSource));
                case ArchitectureDiagram -> jigService.architectureDiagram(jigSource);
                case DomainSummary -> SummaryModel.from(jigService.businessRules(jigSource));
                case ApplicationSummary -> SummaryModel.from(jigService.serviceMethods(jigSource));
                case UsecaseSummary -> usecaseSummary(jigSource);
                case EntrypointSummary -> entrypointSummary(jigSource);
                case EnumSummary -> SummaryModel.from(jigService.categoryTypes(jigSource), jigSource.enumModels());
                case TermTable -> jigService.terms(jigSource);
                case TermList ->
                        new ModelReports(new ModelReport<>(jigService.terms(jigSource).list(), TermReport::new, TermReport.class));
            };

            JigView jigView = switch (jigDocument.jigDocumentType()) {
                case LIST -> new ModelReportsPoiView(jigDocumentContext);
                case DIAGRAM -> new DotView(diagramFormat, dotCommandRunner, jigDocumentContext);
                case SUMMARY -> new SummaryView(templateEngine, jigDocumentContext);
                case TABLE -> new TableView(templateEngine);
            };

            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);
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

    private ModelReports domainList(JigSource jigSource) {
        var typeFacts = jigSource.typeFacts();

        MethodSmellList methodSmellList = jigService.methodSmells(jigSource);
        JigTypes jigTypes = jigService.jigTypes(jigSource);

        BusinessRules businessRules = jigService.businessRules(jigSource);

        CategoryDiagram categoryDiagram = jigService.categories(jigSource);
        List<BusinessRulePackage> businessRulePackages = jigService.businessRules(jigSource).listPackages();
        return new ModelReports(
                new ModelReport<>(businessRulePackages, PackageReport::new, PackageReport.class),
                new ModelReport<>(businessRules.list(),
                        businessRule -> new BusinessRuleReport(businessRule, businessRules),
                        BusinessRuleReport.class),
                new ModelReport<>(categoryDiagram.list(), CategoryReport::new, CategoryReport.class),
                new ModelReport<>(businessRules.jigTypes().listCollectionType(),
                        jigType -> new CollectionReport(jigType, typeFacts.toClassRelations()),
                        CollectionReport.class),
                new ModelReport<>(Validations.from(jigTypes).list(), ValidationReport::new, ValidationReport.class),
                new ModelReport<>(methodSmellList.list(), MethodSmellReport::new, MethodSmellReport.class)
        );
    }

    private ModelReports applicationList(JigSource jigSource) {
        ServiceAngles serviceAngles = jigService.serviceAngles(jigSource);

        DatasourceAngles datasourceAngles = jigService.datasourceAngles(jigSource);
        StringComparingMethodList stringComparingMethodList = jigService.stringComparing(jigSource);

        var entrypoint = jigService.entrypoint(jigSource);
        if (entrypoint.isEmpty()) {
            logger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知.localizedMessage());
        }

        return new ModelReports(
                new ModelReport<>(entrypoint.listRequestHandlerMethods(),
                        requestHandlerMethod -> new ControllerReport(requestHandlerMethod),
                        ControllerReport.class),
                new ModelReport<>(serviceAngles.list(),
                        serviceAngle -> new ServiceReport(serviceAngle),
                        ServiceReport.class),
                new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class),
                new ModelReport<>(stringComparingMethodList.list(), StringComparingReport::new, StringComparingReport.class)
        );
    }

    private SummaryModel usecaseSummary(JigSource jigSource) {
        ServiceAngles serviceAngles = jigService.serviceAngles(jigSource);

        record Entry(JigMethod jigMethod, String mermaidText) {
        }

        var mermaidMap = serviceAngles.list()
                .stream()
                .map(rootServiceMethod -> {
                    return new Entry(
                            rootServiceMethod.serviceMethod().method(),
                            serviceAngles.mermaidText(rootServiceMethod.method().identifier())
                    );
                })
                .collect(Collectors.groupingBy(
                        entry -> entry.jigMethod.fqn(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream().findFirst().map(entry -> entry.mermaidText()).orElse(null))
                ));

        return SummaryModel.from(jigService.serviceMethods(jigSource), mermaidMap);
    }

    private SummaryModel entrypointSummary(JigSource jigSource) {
        return SummaryModel.from(jigService.entrypoint(jigSource));
    }
}
