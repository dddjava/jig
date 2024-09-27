package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.presentation.handler.JigExecutor;
import org.dddjava.jig.presentation.handler.HandleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JIG-CLI Runner
 */
@Component
class CliRunner {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CliConfig cliConfig;

    CliRunner(CliConfig cliConfig) {
        this.cliConfig = cliConfig;
    }

    void run(String... args) {
        Configuration configuration = cliConfig.configuration();
        logger.info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", cliConfig.propertiesText());

        long startTime = System.currentTimeMillis();
        SourcePaths sourcePaths = cliConfig.rawSourceLocations();

        List<HandleResult> handleResultList = JigExecutor.execute(configuration, sourcePaths);

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(Collectors.joining("\n"));
        logger.info("-- Output Complete {} ms -------------------------------------------\n{}\n------------------------------------------------------------",
                System.currentTimeMillis() - startTime,
                resultLog);
    }
}
