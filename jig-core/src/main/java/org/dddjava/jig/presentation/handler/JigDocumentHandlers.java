package org.dddjava.jig.presentation.handler;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.documents.diagrams.*;
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
import org.dddjava.jig.domain.model.parts.term.Terms;
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
    private final DependencyService dependencyService;
    private final BusinessRuleService businessRuleService;
    private final ApplicationService applicationService;

    public JigDocumentHandlers(JigDocumentContext jigDocumentContext,
                               JigDiagramFormat diagramFormat,
                               DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService,
                               List<JigDocument> jigDocuments,
                               Path outputDirectory) {
        this.dependencyService = dependencyService;
        this.businessRuleService = businessRuleService;
        this.applicationService = applicationService;
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

    public static JigDocumentHandlers from(JigDocumentContext jigDocumentContext, DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService, JigDiagramFormat outputDiagramFormat, List<JigDocument> jigDocuments, Path outputDirectory) {
        return new JigDocumentHandlers(
                jigDocumentContext,
                outputDiagramFormat,
                dependencyService, businessRuleService, applicationService,
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
            Object model = handle(jigDocument);

            JigView jigView = resolve(jigDocument);
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
        IndexView indexView = indexView();
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

    private JigView resolve(JigDocument jigDocument) {
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

    private IndexView indexView() {
        return new IndexView(templateEngine, diagramFormat);
    }

    public PackageRelationDiagram packageDependency() {
        return dependencyService.packageDependencies();
    }

    public ClassRelationDiagram businessRuleRelation() {
        return new ClassRelationDiagram(dependencyService.businessRules());
    }

    public ClassRelationCoreDiagram coreBusinessRuleRelation() {
        return new ClassRelationCoreDiagram(new ClassRelationDiagram(dependencyService.businessRules()));
    }

    public ClassRelationConcentrateDiagram overconcentrationBusinessRuleRelation() {
        return new ClassRelationConcentrateDiagram(dependencyService.businessRules());
    }

    public CategoryUsageDiagram categoryUsage() {
        return businessRuleService.categoryUsages();
    }

    public CategoryDiagram categories() {
        return businessRuleService.categories();
    }

    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy() {
        return applicationService.serviceMethodCallHierarchy();
    }

    public ArchitectureDiagram architecture() {
        return applicationService.architectureDiagram();
    }

    public CompositeUsecaseDiagram useCaseDiagram() {
        return new CompositeUsecaseDiagram(applicationService.serviceAngles());
    }

    public ModelReports termList() {
        Terms terms = businessRuleService.terms();
        return new ModelReports(new ModelReport<>(terms.list(), TermReport::new, TermReport.class));
    }

    public SummaryModel domainListHtml() {
        return SummaryModel.from(businessRuleService.businessRules());
    }

    public SummaryModel enumListHtml() {
        return SummaryModel.from(businessRuleService.categoryTypes(), businessRuleService.enumModels());
    }

    public ModelReports domainList() {
        MethodSmellList angles = businessRuleService.methodSmells();
        JigTypes jigTypes = businessRuleService.jigTypes();

        JigCollectionTypes jigCollectionTypes = businessRuleService.collections();
        CategoryDiagram categoryDiagram = businessRuleService.categories();
        BusinessRules businessRules = businessRuleService.businessRules();
        BusinessRulePackages businessRulePackages = businessRuleService.businessRules().businessRulePackages();
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

    public ModelReports applicationList() {
        ServiceAngles serviceAngles = applicationService.serviceAngles();

        DatasourceAngles datasourceAngles = applicationService.datasourceAngles();
        StringComparingMethodList stringComparingMethodList = applicationService.stringComparing();
        HandlerMethods handlerMethods = applicationService.controllerAngles();

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

    public SummaryModel applicationSummary() {
        return SummaryModel.from(applicationService.serviceMethods());
    }

    public SummaryModel usecaseSummary() {
        ServiceAngles serviceAngles = applicationService.serviceAngles();

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

        return SummaryModel.from(applicationService.serviceMethods(), mermaidMap);
    }

    public Object handle(JigDocument jigDocument) {
        return switch (jigDocument) {
            case BusinessRuleList -> domainList();
            case PackageRelationDiagram -> packageDependency();
            case BusinessRuleRelationDiagram -> businessRuleRelation();
            case OverconcentrationBusinessRuleDiagram -> overconcentrationBusinessRuleRelation();
            case CoreBusinessRuleRelationDiagram -> coreBusinessRuleRelation();
            case CategoryDiagram -> categories();
            case CategoryUsageDiagram -> categoryUsage();
            case ApplicationList -> applicationList();
            case ServiceMethodCallHierarchyDiagram -> serviceMethodCallHierarchy();
            case CompositeUsecaseDiagram -> useCaseDiagram();
            case ArchitectureDiagram -> architecture();
            case DomainSummary -> domainListHtml();
            case ApplicationSummary -> applicationSummary();
            case UsecaseSummary -> usecaseSummary();
            case EnumSummary -> enumListHtml();
            case TermTable -> businessRuleService.terms();
            case TermList -> termList();
        };

    }
}
