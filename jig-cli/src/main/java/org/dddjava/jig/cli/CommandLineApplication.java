package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.basic.ClassFindFailException;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.LocalProject;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentHandler;
import org.dddjava.jig.presentation.view.JigHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

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
    ImplementationService implementationService;
    @Autowired
    LocalProject localProject;

    @Autowired
    JigHandlerContext jigHandlerContext;

    @Autowired
    Environment environment;

    @Override
    public void run(String... args) {
        long startTime = System.currentTimeMillis();
        try {
            List<JigDocument> jigDocuments =
                    documentTypeText.isEmpty()
                            ? Arrays.asList(JigDocument.values())
                            : JigDocument.resolve(documentTypeText);

            LOGGER.info("プロジェクト情報の取り込みをはじめます");
            ProjectData projectData = implementationService.readProjectData(localProject);

            jigHandlerContext.setProjectData(projectData);

            Path outputDirectory = Paths.get(this.outputDirectory);
            for (JigDocument jigDocument : jigDocuments) {
                JigDocumentHandler.of(jigDocument)
                        .handleLocal(jigHandlerContext)
                        .render(outputDirectory);
            }
        } catch (ClassFindFailException e) {
            LOGGER.warn(e.warning().textWithSpringEnvironment(environment));
        }
        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}
