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

    public void generateIndex(List<HandleResult> results) {
        IndexView indexView = new IndexView(templateEngine, diagramFormat);
        indexView.render(results, outputDirectory);
        copyAssets(outputDirectory);
    }

    public List<HandleResult> generateDocuments(JigSource jigSource) {
        prepareOutputDirectory();

        return jigDocuments
                .parallelStream()
                .map(jigDocument -> generateDocument(jigDocument, outputDirectory, jigSource))
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

    private HandleResult generateDocument(JigDocument jigDocument, Path outputDirectory, JigSource jigSource) {
        try {
            long startTime = System.currentTimeMillis();

            Object model = switch (jigDocument) {
                // overview
                case PackageRelationDiagram -> jigService.packageDependencies(jigSource);
                case CompositeUsecaseDiagram -> new CompositeUsecaseDiagram(jigService.serviceAngles(jigSource));
                case ArchitectureDiagram -> jigService.architectureDiagram(jigSource);
                case TermTable -> jigService.terms(jigSource);
                case TermList ->
                        new ModelReports(new ModelReport<>(jigService.terms(jigSource).list(), TermReport::new, TermReport.class));

                // domain
                case BusinessRuleRelationDiagram -> new ClassRelationDiagram(jigService.businessRules(jigSource));
                case DomainSummary ->
                        SummaryModel.from(jigService.jigTypes(jigSource), jigService.businessRules(jigSource));
                case BusinessRuleList -> domainList(jigSource);
                case CategoryDiagram -> jigService.categories(jigSource);
                case CategoryUsageDiagram -> jigService.categoryUsages(jigSource);
                case EnumSummary ->
                        SummaryModel.from(jigService.jigTypes(jigSource), jigService.categoryTypes(jigSource), jigSource.enumModels());

                // application & usecase
                case ApplicationList -> applicationList(jigSource);
                case ApplicationSummary, UsecaseSummary -> SummaryModel.from(jigService.services(jigSource));
                case ServiceMethodCallHierarchyDiagram -> jigService.serviceMethodCallHierarchy(jigSource);
                case EntrypointSummary ->
                        SummaryModel.from(jigService.jigTypes(jigSource), jigService.entrypoint(jigSource));
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
}
