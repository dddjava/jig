package org.dddjava.jig.application;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.TypeVisibility;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.ClassRelationDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.information.domains.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.information.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.information.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.validations.Validations;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.dddjava.jig.infrastructure.view.graphviz.dot.GraphvizDiagramWriter;
import org.dddjava.jig.infrastructure.view.html.IndexView;
import org.dddjava.jig.infrastructure.view.html.JigExpressionObjectDialect;
import org.dddjava.jig.infrastructure.view.html.SummaryView;
import org.dddjava.jig.infrastructure.view.html.TableView;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JigDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentGenerator.class);

    private final JigDocumentContext jigDocumentContext;
    private final JigDiagramFormat diagramFormat;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    private final TemplateEngine thymeleafTemplateEngine;
    private final JigService jigService;
    private final GraphvizDiagramWriter graphvizDiagramWriter;

    public JigDocumentGenerator(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
        this.diagramFormat = jigDocumentContext.diagramFormat();
        this.jigDocuments = jigDocumentContext.jigDocuments();
        this.outputDirectory = jigDocumentContext.outputDirectory();

        this.graphvizDiagramWriter = new GraphvizDiagramWriter(jigDocumentContext);

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
                case DomainSummary -> {
                    var summaryModel = jigService.domainSummary(jigSource);
                    yield new SummaryView(thymeleafTemplateEngine, jigDocumentContext).write(jigDocument, summaryModel);
                }
                case ApplicationSummary, UsecaseSummary -> {
                    var summaryModel = jigService.usecaseSummary(jigSource);
                    yield new SummaryView(thymeleafTemplateEngine, jigDocumentContext).write(jigDocument, summaryModel);
                }
                case EntrypointSummary -> {
                    var summaryModel = jigService.inputsSummary(jigSource);
                    yield new SummaryView(thymeleafTemplateEngine, jigDocumentContext).write(jigDocument, summaryModel);
                }
                case EnumSummary -> {
                    var summaryModel = SummaryModel.from(jigService.jigTypes(jigSource), jigService.categoryTypes(jigSource), jigSource.enumModels());
                    yield new SummaryView(thymeleafTemplateEngine, jigDocumentContext).write(jigDocument, summaryModel);
                }
                // テーブル
                case TermTable -> {
                    var terms = jigService.terms(jigSource);
                    yield new TableView(jigDocument, thymeleafTemplateEngine).write(outputDirectory, terms);
                }
                // ダイアグラム
                case PackageRelationDiagram -> {
                    var diagram = jigService.packageDependencies(jigSource);
                    yield graphvizDiagramWriter.write(diagram, jigDocument);
                }
                case CompositeUsecaseDiagram -> {
                    var diagram = new CompositeUsecaseDiagram(jigService.serviceAngles(jigSource));
                    yield graphvizDiagramWriter.write(diagram, jigDocument);
                }
                case ArchitectureDiagram -> {
                    var diagram = jigService.architectureDiagram(jigSource);
                    yield graphvizDiagramWriter.write(diagram, jigDocument);
                }
                case BusinessRuleRelationDiagram -> {
                    var diagram = new ClassRelationDiagram(jigService.businessRules(jigSource));
                    yield graphvizDiagramWriter.write(diagram, jigDocument);
                }
                case CategoryDiagram -> {
                    var diagram = jigService.categories(jigSource);
                    yield graphvizDiagramWriter.write(diagram, jigDocument);
                }
                case CategoryUsageDiagram -> {
                    var diagram = jigService.categoryUsages(jigSource);
                    yield graphvizDiagramWriter.write(diagram, jigDocument);
                }
                case ServiceMethodCallHierarchyDiagram -> {
                    var diagram = jigService.serviceMethodCallHierarchy(jigSource);
                    yield graphvizDiagramWriter.write(diagram, jigDocument);
                }
                // 一覧
                case TermList -> {
                    Terms terms = jigService.terms(jigSource);
                    var modelReports = new ReportBook(new ReportSheet<>("TERM", Terms.reporter(), terms.list()));
                    yield modelReports.writeXlsx(jigDocument, outputDirectory);
                }
                case BusinessRuleList -> {
                    var modelReports = businessRuleReports(jigSource.typeFacts(), jigService.methodSmells(jigSource), jigService.jigTypes(jigSource), jigService.businessRules(jigSource), jigService.categories(jigSource), jigService.businessRules(jigSource).listPackages());
                    yield modelReports.writeXlsx(jigDocument, outputDirectory);
                }
                case ApplicationList -> {
                    var modelReports = applicationReports(jigService.serviceAngles(jigSource), jigService.datasourceAngles(jigSource), jigService.stringComparing(jigSource), jigService.entrypoint(jigSource));
                    yield modelReports.writeXlsx(jigDocument, outputDirectory);
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

    private ReportBook businessRuleReports(TypeFacts typeFacts, MethodSmellList methodSmellList, JigTypes jigTypes, BusinessRules businessRules, CategoryDiagram categoryDiagram, List<BusinessRulePackage> businessRulePackages) {
        return new ReportBook(
                new ReportSheet<>("PACKAGE", List.of(
                        Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                        Map.entry("パッケージ別名", item -> jigDocumentContext.packageComment(item.packageIdentifier()).asText()),
                        Map.entry("クラス数", item -> item.businessRules().list().size())
                ), businessRulePackages),
                new ReportSheet<>("ALL", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
                        Map.entry("ビジネスルールの種類", item -> item.toValueKind().toString()),
                        Map.entry("関連元ビジネスルール数", item -> businessRules.businessRuleRelations().filterTo(item.typeIdentifier()).fromTypeIdentifiers().size()),
                        Map.entry("関連先ビジネスルール数", item -> businessRules.businessRuleRelations().filterFrom(item.typeIdentifier()).toTypeIdentifiers().size()),
                        Map.entry("関連元クラス数", item -> businessRules.allTypesRelatedTo(item).list().size()),
                        Map.entry("非PUBLIC", item -> item.visibility() != TypeVisibility.PUBLIC ? "◯" : ""),
                        Map.entry("同パッケージからのみ参照", item -> {
                            var list = businessRules.allTypesRelatedTo(item).packageIdentifiers().list();
                            return list.size() == 1 && list.get(0).equals(item.typeIdentifier().packageIdentifier()) ? "◯" : "";
                        }),
                        Map.entry("関連元クラス", item -> businessRules.allTypesRelatedTo(item).asSimpleText())
                ), businessRules.list()),
                new ReportSheet<>("ENUM", List.of(
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
                ), categoryDiagram.list()),
                new ReportSheet<>("COLLECTION", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
                        Map.entry("フィールドの型", item -> item.instanceMember().fieldDeclarations().onlyOneField().fieldType().asSimpleText()), // TODO: onlyOne複数に対応する。型引数を出力したいのでFieldTypeを使用している。
                        Map.entry("使用箇所数", item -> typeFacts.toClassRelations().collectTypeIdentifierWhichRelationTo(item.identifier()).size()),
                        Map.entry("使用箇所", item -> typeFacts.toClassRelations().collectTypeIdentifierWhichRelationTo(item.identifier()).asSimpleText()),
                        Map.entry("メソッド数", item -> item.instanceMember().instanceMethods().list().size()),
                        Map.entry("メソッド一覧", item -> item.instanceMember().instanceMethods().declarations().asSignatureAndReturnTypeSimpleText())
                ), businessRules.jigTypes().listCollectionType()),
                new ReportSheet<>("VALIDATION", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
                        Map.entry("メンバ名", item -> item.memberName()),
                        Map.entry("メンバクラス名", item -> item.memberType().asSimpleText()),
                        Map.entry("アノテーションクラス名", item -> item.annotationType().asSimpleText()),
                        Map.entry("アノテーション記述", item -> item.annotationDescription())
                ), Validations.from(jigTypes).list()),
                new ReportSheet<>("注意メソッド", List.of(
                        Map.entry("パッケージ名", item -> item.methodDeclaration().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.methodDeclaration().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.methodDeclaration().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.methodDeclaration().methodReturn().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.methodDeclaration().declaringType()).asText()),
                        Map.entry("使用箇所数", item -> item.callerMethods().size()),
                        Map.entry("メンバを使用していない", item -> item.notUseMember() ? "◯" : ""),
                        Map.entry("基本型の授受を行なっている", item -> item.primitiveInterface() ? "◯" : ""),
                        Map.entry("NULLリテラルを使用している", item -> item.referenceNull() ? "◯" : ""),
                        Map.entry("NULL判定をしている", item -> item.nullDecision() ? "◯" : ""),
                        Map.entry("真偽値を返している", item -> item.returnsBoolean() ? "◯" : ""),
                        Map.entry("voidを返している", item -> item.returnsVoid() ? "◯" : "")
                ), methodSmellList.list())
        );
    }

    private ReportBook applicationReports(ServiceAngles serviceAngles, DatasourceAngles datasourceAngles, StringComparingMethodList stringComparingMethodList, Entrypoint entrypoint) {
        if (entrypoint.isEmpty()) {
            logger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知.localizedMessage());
        }

        return new ReportBook(
                new ReportSheet<>("CONTROLLER", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.method().declaration().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.method().declaration().methodReturn().asSimpleText()),
                        Map.entry("クラス別名", item -> item.jigType().typeAlias().asText()),
                        Map.entry("使用しているフィールドの型", item -> item.method().usingFields().typeIdentifiers().asSimpleText()),
                        Map.entry("分岐数", item -> item.method().decisionNumber().intValue()),
                        Map.entry("パス", item -> item.pathText())
                ), entrypoint.listRequestHandlerMethods()),
                new ReportSheet<>("SERVICE", List.of(
                        Map.entry("パッケージ名", item -> item.serviceMethod().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.serviceMethod().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.method().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.method().methodReturn().asSimpleText()),
                        Map.entry("イベントハンドラ", item -> item.usingFromController() ? "◯" : ""),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.serviceMethod().declaringType()).asText()),
                        Map.entry("メソッド別名", item -> item.serviceMethod().method().aliasTextOrBlank()),
                        Map.entry("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.classComment(item.serviceMethod().method().declaration().methodReturn().typeIdentifier()).asText()
                        ),
                        Map.entry("メソッド引数の型の別名", item ->
                                item.serviceMethod().method().declaration().methodSignature().listArgumentTypeIdentifiers().stream()
                                        .map(jigDocumentContext::classComment)
                                        .map(ClassComment::asText)
                                        .collect(Collectors.joining(", ", "[", "]"))
                        ),
                        Map.entry("使用しているフィールドの型", item -> item.usingFields().typeIdentifiers().asSimpleText()),
                        Map.entry("分岐数", item -> item.serviceMethod().method().decisionNumber().intValue()),
                        Map.entry("使用しているサービスのメソッド", item -> item.usingServiceMethods().asSignatureAndReturnTypeSimpleText()),
                        Map.entry("使用しているリポジトリのメソッド", item -> item.usingRepositoryMethods().asSimpleText()),
                        Map.entry("null使用", item -> item.useNull() ? "◯" : ""),
                        Map.entry("stream使用", item -> item.useStream() ? "◯" : "")
                ), serviceAngles.list()),
                new ReportSheet<>("REPOSITORY", List.of(
                        Map.entry("パッケージ名", item -> item.method().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.method().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.method().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.method().methodReturn().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.method().declaringType()).asText()),
                        Map.entry("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.classComment(item.method().methodReturn().typeIdentifier()).asText()
                        ),
                        Map.entry("メソッド引数の型の別名", item ->
                                item.method().methodSignature().listArgumentTypeIdentifiers().stream()
                                        .map(jigDocumentContext::classComment)
                                        .map(ClassComment::asText)
                                        .collect(Collectors.joining(", ", "[", "]"))
                        ),
                        Map.entry("分岐数", item -> item.concreteMethod().decisionNumber().intValue()),
                        Map.entry("INSERT", item -> item.insertTables()),
                        Map.entry("SELECT", item -> item.selectTables()),
                        Map.entry("UPDATE", item -> item.updateTables()),
                        Map.entry("DELETE", item -> item.deleteTables()),
                        Map.entry("関連元クラス数", item -> item.callerMethods().toDeclareTypes().size()),
                        Map.entry("関連元メソッド数", item -> item.callerMethods().size())
                ), datasourceAngles.list()),
                new ReportSheet<>("文字列比較箇所", List.of(
                        Map.entry("パッケージ名", item2 -> item2.declaration().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item2 -> item2.declaration().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item2 -> item2.declaration().asSignatureSimpleText())
                ), stringComparingMethodList.list())
        );
    }
}
