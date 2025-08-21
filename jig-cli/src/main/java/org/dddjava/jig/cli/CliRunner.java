package org.dddjava.jig.cli;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static java.util.stream.Collectors.joining;

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

    void run() {
        Configuration configuration = cliConfig.configuration();
        logger.info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", cliConfig.propertiesText());

        long startTime = System.currentTimeMillis();
        SourceBasePaths sourceBasePaths = cliConfig.rawSourceLocations();

        List<HandleResult> handleResultList = JigExecutor.execute(configuration, sourceBasePaths);

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(joining("\n"));
        logger.info("-- Output Complete {} ms -------------------------------------------\n{}\n------------------------------------------------------------",
                System.currentTimeMillis() - startTime,
                resultLog);
    }
}
