package org.dddjava.jig.gradle;

import org.dddjava.jig.application.service.JigSourceReadService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.jigreader.SourceCodeAliasReader;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;
import org.dddjava.jig.presentation.controller.JigExecutor;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.nio.file.Paths;
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
        JigSourceReadService jigSourceReadService = configuration.implementationService();
        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();
        SourcePaths sourcePaths = new GradleProject(project).rawSourceLocations();
        Path outputDirectory = outputDirectory(config);

        List<HandleResult> handleResultList =
                JigExecutor.execute(jigDocuments, jigSourceReadService, jigDocumentHandlers, sourcePaths, outputDirectory, getLogger());

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
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
