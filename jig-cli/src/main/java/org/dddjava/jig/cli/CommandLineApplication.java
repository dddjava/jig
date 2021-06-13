package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.presentation.controller.JigExecutor;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class CommandLineApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CommandLineApplication.class, args);
    }

    @Autowired
    CliConfig cliConfig;

    @Override
    public void run(String... args) {
        Configuration configuration = cliConfig.configuration();

        LOGGER.info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", cliConfig.propertiesText());

        long startTime = System.currentTimeMillis();
        SourcePaths sourcePaths = cliConfig.rawSourceLocations();

        List<HandleResult> handleResultList = JigExecutor.execute(configuration, sourcePaths, LOGGER);

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(Collectors.joining("\n"));
        LOGGER.info("-- output documents -------------------------------------------\n{}\n------------------------------------------------------------", resultLog);
        LOGGER.info("Finished: {} ms", System.currentTimeMillis() - startTime);
    }
}
