package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.JigSourceReadService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigsource.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;
import org.dddjava.jig.presentation.controller.JigExecutor;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@SpringBootApplication
public class KotlinCommandLineApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(KotlinCommandLineApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KotlinCommandLineApplication.class, args);
    }

    @Autowired
    KotlinCliConfig cliConfig;

    @Override
    public void run(String... args) {
        ResourceBundle jigMessages = Utf8ResourceBundle.messageBundle();
        List<JigDocument> jigDocuments = cliConfig.jigDocuments();
        Configuration configuration = cliConfig.configuration();

        LOGGER.info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", cliConfig.propertiesText());

        long startTime = System.currentTimeMillis();
        JigSourceReadService jigSourceReadService = configuration.implementationService();
        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();
        SourcePaths sourcePaths = cliConfig.rawSourceLocations();
        Path outputDirectory = configuration.outputDirectory();

        List<HandleResult> handleResultList = JigExecutor.execute(jigDocuments, jigSourceReadService, jigDocumentHandlers, sourcePaths, outputDirectory, LOGGER);

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(Collectors.joining("\n"));
        LOGGER.info("-- output documents -------------------------------------------\n{}\n------------------------------------------------------------", resultLog);
        LOGGER.info(jigMessages.getString("success"), System.currentTimeMillis() - startTime);
    }
}
