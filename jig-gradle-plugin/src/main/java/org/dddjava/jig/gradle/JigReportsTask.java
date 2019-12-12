package org.dddjava.jig.gradle;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.implementation.source.SourcePaths;
import org.dddjava.jig.domain.model.interpret.alias.SourceCodeAliasReader;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzeStatus;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzeStatuses;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class JigReportsTask extends DefaultTask {

    @TaskAction
    void outputReports() {
        ResourceBundle jigMessages = Utf8ResourceBundle.messageBundle();
        Project project = getProject();
        JigConfig config = project.getExtensions().findByType(JigConfig.class);

        List<JigDocument> jigDocuments = config.documentTypes();
        Configuration configuration = new Configuration(config.asProperties(), new SourceCodeAliasReader(new JavaparserAliasReader()));

        getLogger().info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", config.propertiesText());

        long startTime = System.currentTimeMillis();
        ImplementationService implementationService = configuration.implementationService();
        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

        SourcePaths sourcePaths = new GradleProject(project).rawSourceLocations();
        AnalyzedImplementation implementations = implementationService.implementations(sourcePaths);

        AnalyzeStatuses status = implementations.status();
        if (status.hasError()) {
            getLogger().warn(jigMessages.getString("failure"));
            for (AnalyzeStatus analyzeStatus : status.listErrors()) {
                getLogger().warn(jigMessages.getString("failure.details"), jigMessages.getString(analyzeStatus.messageKey));
            }
            return;
        }
        if (status.hasWarning()) {
            getLogger().warn(jigMessages.getString("implementation.warning"));
            for (AnalyzeStatus analyzeStatus : status.listWarning()) {
                getLogger().warn(jigMessages.getString("implementation.warning.details"), jigMessages.getString(analyzeStatus.messageKey));
            }
        }

        List<HandleResult> handleResultList = new ArrayList<>();
        Path outputDirectory = outputDirectory(config);
        for (JigDocument jigDocument : jigDocuments) {
            HandleResult result = jigDocumentHandlers.handle(jigDocument, new HandlerMethodArgumentResolver(implementations), outputDirectory);
            handleResultList.add(result);
        }

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePaths())
                .collect(Collectors.joining("\n"));
        getLogger().info("-- output documents -------------------------------------------\n{}\n------------------------------------------------------------", resultLog);
        getLogger().info(jigMessages.getString("success"), System.currentTimeMillis() - startTime);
    }

    Path outputDirectory(JigConfig config) {
        Project project = getProject();
        Path path = Paths.get(config.getOutputDirectory());
        if (path.isAbsolute()) return path;

        return project.getBuildDir().toPath().resolve("jig");
    }
}
