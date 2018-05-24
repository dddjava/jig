package org.dddjava.jig.cli;

import org.dddjava.jig.application.usecase.ImportService;
import org.dddjava.jig.domain.model.DocumentType;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.presentation.view.JigDocumentHandler;
import org.dddjava.jig.presentation.view.JigHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication(scanBasePackages = "org.dddjava.jig")
public class CommandLineApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CommandLineApplication.class, args);
    }

    @Value("${documentType:}")
    String documentTypeText;
    @Value("${outputDirectory}")
    String outputDirectory;

    @Autowired
    ImportService importService;
    @Autowired
    LocalProject localProject;

    @Autowired
    JigHandlerContext jigHandlerContext;

    @Override
    public void run(String... args) throws IOException {
        List<DocumentType> documentTypes =
                documentTypeText.isEmpty()
                        ? Arrays.asList(DocumentType.values())
                        : DocumentType.resolve(documentTypeText);


        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        importService.importSources(
                localProject.getSpecificationSources(),
                localProject.getSqlSources(),
                localProject.getTypeNameSources(),
                localProject.getPackageNameSources());

        Path outputDirectory = Paths.get(this.outputDirectory);
        for (DocumentType documentType : documentTypes) {
            JigDocumentHandler.of(documentType)
                    .handleLocal(jigHandlerContext)
                    .render(outputDirectory);
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}
