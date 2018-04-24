package jig.diagram;

import jig.application.service.AngleService;
import jig.application.service.DependencyService;
import jig.application.usecase.ImportLocalProjectService;
import jig.diagram.graphvizj.GraphvizJavaDriver;
import jig.diagram.graphvizj.ServiceMethodCallHierarchyWriter;
import jig.domain.basic.FileWriteFailureException;
import jig.domain.model.angle.ServiceAngles;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${diagramType:}")
    String diagramTypeText;
    @Value("${depth:-1}")
    int depth;

    @Autowired
    ImportLocalProjectService importLocalProjectService;
    @Autowired
    AngleService angleService;

    @Autowired
    DependencyService dependencyService;
    @Autowired
    JapaneseNameRepository japaneseNameRepository;
    @Autowired
    PackageIdentifierFormatter packageIdentifierFormatter;

    @Override
    public void run(String... args) {
        DiagramType diagramType = DiagramType.resolve(diagramTypeText);

        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        importLocalProjectService.importProject();

        if (diagramType == DiagramType.ServiceMethodCallHierarchy) {
            serviceMethodCallHierarchy();
        } else if (diagramType == DiagramType.PackageDependency) {
            packageDependency();
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }

    private void packageDependency() {
        LOGGER.info("パッケージ依存情報を取得します(設定深度: {})", this.depth);
        PackageDependencies packageDependencies = dependencyService.packageDependencies()
                .applyDepth(new PackageDepth(this.depth));

        LOGGER.info("出力する関連数: {}", packageDependencies.number().asText());

        Path path = Paths.get("jig-diagram_package-dependency.png");
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            new GraphvizJavaDriver(packageIdentifierFormatter, japaneseNameRepository).write(packageDependencies, outputStream);

            LOGGER.info("{} を出力しました。", path);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }

    private void serviceMethodCallHierarchy() {
        LOGGER.info("ServiceAngleを取得します");
        ServiceAngles serviceAngles = angleService.serviceAngles();

        Path path = Paths.get("jig-diagram_service-method-call-hierarchy.png");
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            new ServiceMethodCallHierarchyWriter().write(serviceAngles, outputStream);

            LOGGER.info("{} を出力しました。", path);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }
}
