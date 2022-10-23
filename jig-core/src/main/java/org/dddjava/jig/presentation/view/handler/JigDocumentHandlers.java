package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.presentation.controller.ApplicationListController;
import org.dddjava.jig.presentation.controller.BusinessRuleListController;
import org.dddjava.jig.presentation.controller.DiagramController;
import org.dddjava.jig.presentation.view.html.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JigDocumentHandlers {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentHandlers.class);

    private final ViewResolver viewResolver;
    private final BusinessRuleListController businessRuleListController;
    private final ApplicationListController applicationListController;
    private final DiagramController diagramController;

    public JigDocumentHandlers(ViewResolver viewResolver,
                               BusinessRuleListController businessRuleListController,
                               ApplicationListController applicationListController,
                               DiagramController diagramController) {
        this.viewResolver = viewResolver;

        this.businessRuleListController = businessRuleListController;
        this.applicationListController = applicationListController;
        this.diagramController = diagramController;
    }

    public List<HandleResult> handleJigDocuments(List<JigDocument> jigDocuments, Path outputDirectory) {
        long startTime = System.currentTimeMillis();
        logger.info("JIGドキュメントを出力します。");
        List<HandleResult> handleResultList = jigDocuments
                .parallelStream()
                .map(jigDocument -> handle(jigDocument, outputDirectory))
                .collect(Collectors.toList());
        writeIndexHtml(outputDirectory, handleResultList);
        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("すべてのJIGドキュメントの出力完了: {} ms", takenTime);
        return handleResultList;
    }

    HandleResult handle(JigDocument jigDocument, Path outputDirectory) {
        try {
            long startTime = System.currentTimeMillis();
            Object model = createModelForJigDocument(jigDocument);

            if (Files.notExists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
                logger.info("{} を作成しました。", outputDirectory.toAbsolutePath());
            }

            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);
            JigView jigView = viewResolver.resolve(jigDocument);
            jigView.render(model, jigDocumentWriter);

            long takenTime = System.currentTimeMillis() - startTime;
            logger.info("{} を {} ms で出力しました。", jigDocument, takenTime);
            return new HandleResult(jigDocument, jigDocumentWriter.outputFilePaths());
        } catch (Exception e) {
            logger.warn("{} の出力に失敗しました。", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }

    private Object createModelForJigDocument(JigDocument jigDocument) {
        // Java17でswitch式に変更
        switch (jigDocument) {
            case BusinessRuleList:
                return businessRuleListController.domainList();
            case PackageRelationDiagram:
                return diagramController.packageDependency();
            case BusinessRuleRelationDiagram:
                return diagramController.businessRuleRelation();
            case OverconcentrationBusinessRuleDiagram:
                return diagramController.overconcentrationBusinessRuleRelation();
            case CoreBusinessRuleRelationDiagram:
                return diagramController.coreBusinessRuleRelation();
            case CategoryDiagram:
                return diagramController.categories();
            case CategoryUsageDiagram:
                return diagramController.categoryUsage();
            case ApplicationList:
                return applicationListController.applicationList();
            case ServiceMethodCallHierarchyDiagram:
                return diagramController.serviceMethodCallHierarchy();
            case CompositeUsecaseDiagram:
                return diagramController.useCaseDiagram();
            case ArchitectureDiagram:
                return diagramController.architecture();
            case ComponentRelationDiagram:
                return diagramController.componentRelation();
            case DomainSummary:
                return businessRuleListController.domainListHtml();
            case ApplicationSummary:
                return applicationListController.applicationSummary();
            case EnumSummary:
                return businessRuleListController.enumListHtml();
            case SchemaSummary:
                return businessRuleListController.schemaHtml();
            case TermList:
                return businessRuleListController.termList();
        }

        throw new IllegalStateException("cannot find handler method for " + jigDocument);
    }

    void writeIndexHtml(Path outputDirectory, List<HandleResult> handleResultList) {
        IndexView indexView = viewResolver.indexView();
        indexView.render(handleResultList, outputDirectory);
        copyAssets(outputDirectory);
    }

    private void copyAssets(Path outputDirectory) {
        Path assetsDirectory = createAssetsDirectory(outputDirectory);
        copyAsset("style.css", assetsDirectory);
        copyAsset("marked.min.js", assetsDirectory);
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
