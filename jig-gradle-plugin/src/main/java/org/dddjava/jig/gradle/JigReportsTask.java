package org.dddjava.jig.gradle;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzeStatuses;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.raw.RawSourceLocations;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.HandlerMethodArgumentResolver;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JigReportsTask extends DefaultTask {

    @TaskAction
    void outputReports() {
        Project project = getProject();
        JigConfig config = project.getExtensions().findByType(JigConfig.class);

        List<JigDocument> jigDocuments = config.documentTypes();
        Configuration configuration = new Configuration(config.asProperties());

        getLogger().info("現在の設定を表示します。\n{}", config.propertiesText());

        long startTime = System.currentTimeMillis();
        getLogger().quiet("プロジェクト情報の取り込みをはじめます");
        ImplementationService implementationService = configuration.implementationService();
        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

        RawSourceLocations rawSourceLocations = new GradleProject(project).rawSourceLocations();
        AnalyzedImplementation implementations = implementationService.implementations(rawSourceLocations);

        AnalyzeStatuses status = implementations.status();
        if (status.hasError()) {
            getLogger().warn("エラーのため出力を中断します。\n{}", status.errorLogText());
            return;
        }
        if (status.hasWarning()) {
            getLogger().warn("読み取りで問題がありました。処理は続行しますが、必要に応じて設定を確認してください。\n{}", status.warningLogText());
        }

        List<HandleResult> handleResultList = new ArrayList<>();
        Path outputDirectory = outputDirectory(config);
        for (JigDocument jigDocument : jigDocuments) {
            HandleResult result = jigDocumentHandlers.handle(jigDocument, new HandlerMethodArgumentResolver(implementations), outputDirectory);
            handleResultList.add(result);
        }
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                getLogger().info("{} を {} に出力しました。", handleResult.jigDocument(), handleResult.outputFilePaths());
            }
        }

        getLogger().quiet("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }

    Path outputDirectory(JigConfig config) {
        Project project = getProject();
        Path path = Paths.get(config.getOutputDirectory());
        if (path.isAbsolute()) return path;

        return project.getBuildDir().toPath().resolve("jig");
    }
}
