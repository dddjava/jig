package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.basic.ClassFindFailException;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

public class Cli {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);

    public static void main(String[] args) {
        CliConfig cliConfig = CommandLine.populateSpec(CliConfig.class, args);
        ExtraScript extraScript = cliConfig.jigCliExtra.equals("api-jpa-crud") ? new ApiJpaCrudListingScript(cliConfig) : projectData -> {
        };

        List<JigDocument> jigDocuments = cliConfig.jigDocuments();
        Configuration configuration = cliConfig.configuration();

        long startTime = System.currentTimeMillis();
        try {
            ImplementationService implementationService = configuration.implementationService();
            LocalProject localProject = configuration.localProject();
            JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

            LOGGER.info("プロジェクト情報の取り込みをはじめます");

            ProjectData projectData = implementationService.readProjectData(localProject);

            Path outputDirectory = cliConfig.outputDirectory();
            for (JigDocument jigDocument : jigDocuments) {
                jigDocumentHandlers.handle(jigDocument, projectData, outputDirectory);
            }

            extraScript.invoke(projectData);
        } catch (ClassFindFailException e) {
            LOGGER.warn(e.warning().with(configuration.configurationContext()));
        }
        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}
