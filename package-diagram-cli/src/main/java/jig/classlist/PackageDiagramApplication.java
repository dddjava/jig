package jig.classlist;

import jig.application.service.DependencyService;
import jig.application.usecase.ImportLocalProjectService;
import jig.diagram.plantuml.PlantumlDriver;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class PackageDiagramApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDiagramApplication.class);

    public static void main(String[] args) {
        System.setProperty("PLANTUML_LIMIT_SIZE", "65536");
        SpringApplication.run(PackageDiagramApplication.class, args);
    }

    @Value("${output.diagram.name}")
    String outputDiagramName;

    @Value("${depth}")
    int depth;

    @Autowired
    ImportLocalProjectService importLocalProjectService;
    @Autowired
    DependencyService dependencyService;
    @Autowired
    PlantumlDriver plantumlDriver;

    @Override
    public void run(String... args) {
        long startTime = System.currentTimeMillis();

        importLocalProjectService.importProject();
        PackageDependencies packageDependencies = dependencyService.packageDependencies()
                .applyDepth(new PackageDepth(this.depth));

        LOGGER.info("出力する関連数: " + packageDependencies.number().asText());

        plantumlDriver.output(packageDependencies, Paths.get(outputDiagramName));

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}

