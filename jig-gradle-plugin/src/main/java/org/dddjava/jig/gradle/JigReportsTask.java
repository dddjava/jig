package org.dddjava.jig.gradle;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.application.service.ClassFindFailException;
import org.dddjava.jig.domain.type.Warning;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.handler.HandlerMethodArgumentResolver;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JigReportsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(JigReportsTask.class);

    @TaskAction
    void outputReports() {
        Project project = getProject();

        ExtensionContainer extensions = project.getExtensions();
        JigConfig config = extensions.findByType(JigConfig.class);

        JigProperties jigProperties = config.asProperties();
        GradleProjects layout = new GradleProject(project).allDependencyJavaProjects();
        JigConfigurationContext configurationContext = new JigConfigurationContext(config);
        Configuration configuration = new Configuration(layout, jigProperties, configurationContext);

        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

        List<JigDocument> jigDocuments = config.documentTypes();

        LocalProject localProject = configuration.localProject();
        ImplementationService implementationService = configuration.implementationService();
        Path outputDirectory = outputDirectory(config);

        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        try {
            TypeByteCodes typeByteCodes = implementationService.readProjectData(localProject);
            Sqls sqls = implementationService.readSql(localProject.getSqlSources());

            for (JigDocument jigDocument : jigDocuments) {
                jigDocumentHandlers.handle(jigDocument, new HandlerMethodArgumentResolver(typeByteCodes, sqls), outputDirectory);
            }
        } catch (ClassFindFailException e) {
            LOGGER.info(Warning.クラス検出異常.with(configurationContext));
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }

    Path outputDirectory(JigConfig config) {
        Project project = getProject();
        Path path = Paths.get(config.getOutputDirectory());
        if (path.isAbsolute()) return path;

        return project.getBuildDir().toPath().resolve("jig");
    }
}
