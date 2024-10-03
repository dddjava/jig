package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.HandleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
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
        Configuration configuration = cliConfig.configuration();

        LOGGER.info("-- configuration -------------------------------------------\n{}\n------------------------------------------------------------", cliConfig.propertiesText());

        long startTime = System.currentTimeMillis();
        SourcePaths sourcePaths = cliConfig.rawSourceLocations();

        List<HandleResult> handleResultList = JigExecutor.execute(configuration, sourcePaths);

        String resultLog = handleResultList.stream()
                .filter(HandleResult::success)
                .map(handleResult -> handleResult.jigDocument() + " : " + handleResult.outputFilePathsText())
                .collect(Collectors.joining("\n"));
        LOGGER.info("-- Output Complete {} ms -------------------------------------------\n{}\n------------------------------------------------------------",
                System.currentTimeMillis() - startTime, resultLog);
    }
}
