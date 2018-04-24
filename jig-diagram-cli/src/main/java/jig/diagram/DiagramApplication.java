package jig.diagram;

import jig.application.service.AngleService;
import jig.application.usecase.ImportLocalProjectService;
import jig.diagram.service.ServiceMethodCallHierarchyWriter;
import jig.domain.basic.FileWriteFailureException;
import jig.domain.model.angle.ServiceAngles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class DiagramApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DiagramApplication.class, args);
    }

    @Autowired
    ImportLocalProjectService importLocalProjectService;
    @Autowired
    AngleService angleService;

    @Override
    public void run(String... args) {
        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        importLocalProjectService.importProject();

        LOGGER.info("ServiceAngleを取得します");
        ServiceAngles serviceAngles = angleService.serviceAngles();

        Path path = Paths.get("jig-diagram-service-method-call-hierarchy.png");
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            new ServiceMethodCallHierarchyWriter().write(serviceAngles, outputStream);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}
