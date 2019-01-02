package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.implementation.ImplementationStatuses;
import org.dddjava.jig.domain.model.implementation.Implementations;
import org.dddjava.jig.domain.model.implementation.raw.RawSourceLocations;
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

        LOGGER.info("現在の設定を表示します。\n{}", cliConfig.propertiesText());

        long startTime = System.currentTimeMillis();
        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        ImplementationService implementationService = configuration.implementationService();
        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

        RawSourceLocations rawSourceLocations = cliConfig.rawSourceLocations();
        Implementations implementations = implementationService.implementations(rawSourceLocations);

        ImplementationStatuses status = implementations.status();
        if (status.hasError()) {
            LOGGER.warn("エラーのため出力を中断します。\n{}", status.errorLogText());
            return;
        }
        if (status.hasWarning()) {
            LOGGER.warn("読み取りで問題がありました。処理は続行しますが、必要に応じて設定を確認してください。\n{}", status.warningLogText());
        }

        Path outputDirectory = cliConfig.outputDirectory();
        for (JigDocument jigDocument : jigDocuments) {
            LOGGER.info("{} を出力します", jigDocument);
            jigDocumentHandlers.handle(jigDocument, new HandlerMethodArgumentResolver(implementations), outputDirectory);

        }
        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}
