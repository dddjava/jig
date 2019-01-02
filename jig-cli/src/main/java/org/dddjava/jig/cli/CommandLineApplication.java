package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.application.service.ClassFindFailException;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.raw.RawSource;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.handler.HandlerMethodArgumentResolver;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.List;

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
        List<JigDocument> jigDocuments = cliConfig.jigDocuments();
        Configuration configuration = cliConfig.configuration();

        long startTime = System.currentTimeMillis();
        try {
            ImplementationService implementationService = configuration.implementationService();
            LocalProject localProject = configuration.localProject();
            JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

            LOGGER.info("プロジェクト情報の取り込みをはじめます");

            RawSource rawSource = localProject.createSource();
            TypeByteCodes typeByteCodes = implementationService.readProjectData(rawSource);

            Sqls sqls = implementationService.readSql(rawSource.sqlSources());

            Path outputDirectory = cliConfig.outputDirectory();
            for (JigDocument jigDocument : jigDocuments) {
                LOGGER.info("{} を出力します。", jigDocument);
                jigDocumentHandlers.handle(jigDocument, new HandlerMethodArgumentResolver(typeByteCodes, sqls), outputDirectory);
            }
        } catch (ClassFindFailException e) {
            LOGGER.warn(e.warning().text());
        }
        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}
