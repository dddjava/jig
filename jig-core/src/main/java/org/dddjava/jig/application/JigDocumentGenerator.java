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
import org.dddjava.jig.domain.model.models.applications.inputs.Entrypoint;
import org.dddjava.jig.domain.model.models.applications.outputs.DatasourceAngles;
import org.dddjava.jig.domain.model.models.applications.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryAngle;
import org.dddjava.jig.domain.model.models.domains.term.Terms;
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
import org.dddjava.jig.infrastructure.view.poi.report.GenericModelReport;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReport;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReports;
import org.dddjava.jig.infrastructure.view.report.business_rule.BusinessRuleReport;
import org.dddjava.jig.infrastructure.view.report.business_rule.CollectionReport;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
                case DomainSummary -> {
                    var summaryModel = jigService.domainSummary(jigSource);
                    yield SummaryView.write(jigDocumentContext, templateEngine, jigDocument, outputDirectory, summaryModel);
                }
                case ApplicationSummary, UsecaseSummary -> {
                    var summaryModel = jigService.usecaseSummary(jigSource);
                    yield SummaryView.write(jigDocumentContext, templateEngine, jigDocument, outputDirectory, summaryModel);
                }
                case EntrypointSummary -> {
                    var summaryModel = jigService.inputsSummary(jigSource);
                    yield SummaryView.write(jigDocumentContext, templateEngine, jigDocument, outputDirectory, summaryModel);
                }
                case EnumSummary -> {
                    var summaryModel = SummaryModel.from(jigService.jigTypes(jigSource), jigService.categoryTypes(jigSource), jigSource.enumModels());
                    yield SummaryView.write(jigDocumentContext, templateEngine, jigDocument, outputDirectory, summaryModel);
                }
                // テーブル
                case TermTable -> {
                    var terms = jigService.terms(jigSource);
                    yield new TableView(jigDocument, templateEngine).write(outputDirectory, terms);
                }
                // ダイアグラム
                case PackageRelationDiagram -> {
                    var diagram = jigService.packageDependencies(jigSource);
                    yield new DotView(jigDocument, diagramFormat, dotCommandRunner, jigDocumentContext).write(outputDirectory, diagram);
                }
                case CompositeUsecaseDiagram -> {
                    var diagram = new CompositeUsecaseDiagram(jigService.serviceAngles(jigSource));
                    yield new DotView(jigDocument, diagramFormat, dotCommandRunner, jigDocumentContext).write(outputDirectory, diagram);
                }
                case ArchitectureDiagram -> {
                    var diagram = jigService.architectureDiagram(jigSource);
                    yield new DotView(jigDocument, diagramFormat, dotCommandRunner, jigDocumentContext).write(outputDirectory, diagram);
                }
                case BusinessRuleRelationDiagram -> {
                    var diagram = new ClassRelationDiagram(jigService.businessRules(jigSource));
                    yield new DotView(jigDocument, diagramFormat, dotCommandRunner, jigDocumentContext).write(outputDirectory, diagram);
                }
                case CategoryDiagram -> {
                    var diagram = jigService.categories(jigSource);
                    yield new DotView(jigDocument, diagramFormat, dotCommandRunner, jigDocumentContext).write(outputDirectory, diagram);
                }
                case CategoryUsageDiagram -> {
                    var diagram = jigService.categoryUsages(jigSource);
                    yield new DotView(jigDocument, diagramFormat, dotCommandRunner, jigDocumentContext).write(outputDirectory, diagram);
                }
                case ServiceMethodCallHierarchyDiagram -> {
                    var diagram = jigService.serviceMethodCallHierarchy(jigSource);
                    yield new DotView(jigDocument, diagramFormat, dotCommandRunner, jigDocumentContext).write(outputDirectory, diagram);
                }
                // 一覧
                case TermList -> {
                    Terms terms = jigService.terms(jigSource);
                    var modelReports = new ModelReports(new GenericModelReport<>("TERM", Terms.reporter(), terms.list()));
                    yield new ModelReportsPoiView(jigDocument, jigDocumentContext).write(outputDirectory, modelReports);
                }
                case BusinessRuleList -> {
                    var modelReports = domainList(jigSource);
                    yield new ModelReportsPoiView(jigDocument, jigDocumentContext).write(outputDirectory, modelReports);
                }
                case ApplicationList -> {
                    var modelReports = applicationList(jigSource);
                    yield new ModelReportsPoiView(jigDocument, jigDocumentContext).write(outputDirectory, modelReports);
                }
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

    private ModelReports domainList(JigSource jigSource) {
        var typeFacts = jigSource.typeFacts();

        MethodSmellList methodSmellList = jigService.methodSmells(jigSource);
        JigTypes jigTypes = jigService.jigTypes(jigSource);

        BusinessRules businessRules = jigService.businessRules(jigSource);

        CategoryDiagram categoryDiagram = jigService.categories(jigSource);
        List<BusinessRulePackage> businessRulePackages = jigService.businessRules(jigSource).listPackages();
        List<Map.Entry<String, Function<BusinessRulePackage, Object>>> packageReporter = List.of(
                Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                Map.entry("パッケージ別名", item -> jigDocumentContext.packageComment(item.packageIdentifier()).asText()),
                Map.entry("クラス数", item -> item.businessRules().list().size())
        );
        List<Map.Entry<String, Function<CategoryAngle, Object>>> categoryReporter = List.of(
                Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
                Map.entry("定数宣言", item -> item.constantsDeclarationsName()),
                Map.entry("フィールド", item -> item.fieldDeclarations()),
                Map.entry("使用箇所数", item -> item.userTypeIdentifiers().list().size()),
                Map.entry("使用箇所", item -> item.userTypeIdentifiers().asSimpleText()),
                Map.entry("パラメーター有り", item -> item.hasParameter() ? "◯" : ""),
                Map.entry("振る舞い有り", item -> item.hasBehaviour() ? "◯" : ""),
                Map.entry("多態", item -> item.isPolymorphism() ? "◯" : "")
        );
        return new ModelReports(
                new GenericModelReport<>("PACKAGE", packageReporter, businessRulePackages),
                ModelReport.createModelReport(businessRules.list(),
                        businessRule -> new BusinessRuleReport(businessRule, businessRules),
                        BusinessRuleReport.class),
                new GenericModelReport<>("ENUM", categoryReporter, categoryDiagram.list()),
                ModelReport.createModelReport(businessRules.jigTypes().listCollectionType(),
                        jigType -> new CollectionReport(jigType, typeFacts.toClassRelations()),
                        CollectionReport.class),
                new GenericModelReport<>("VALIDATION", Validations.reporter(jigDocumentContext), Validations.from(jigTypes).list()),
                new GenericModelReport<>("注意メソッド", MethodSmellList.reporter(jigDocumentContext), methodSmellList.list())
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

        List<Map.Entry<String, Function<JigMethod, Object>>> stringReporter = List.of(
                Map.entry("パッケージ名", item -> item.declaration().declaringType().packageIdentifier().asText()),
                Map.entry("クラス名", item -> item.declaration().declaringType().asSimpleText()),
                Map.entry("メソッドシグネチャ", item -> item.declaration().asSignatureSimpleText())
        );
        return new ModelReports(
                new GenericModelReport<>("CONTROLLER", Entrypoint.reporter(), entrypoint.listRequestHandlerMethods()),
                new GenericModelReport<>("SERVICE", ServiceAngles.reporter(jigDocumentContext), serviceAngles.list()),
                new GenericModelReport<>("REPOSITORY", DatasourceAngles.reporter(jigDocumentContext), datasourceAngles.list()),
                new GenericModelReport<>("文字列比較箇所", stringReporter, stringComparingMethodList.list())
        );
    }
}
