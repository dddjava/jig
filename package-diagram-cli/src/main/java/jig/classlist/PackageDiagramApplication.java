package jig.classlist;

import jig.application.usecase.AnalyzeService;
import jig.diagram.plantuml.PlantumlDriver;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class PackageDiagramApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDiagramApplication.class);

    public static void main(String[] args) {
        System.setProperty("PLANTUML_LIMIT_SIZE", "65536");
        SpringApplication.run(PackageDiagramApplication.class, args);
    }

    @Value("${project.path}")
    String projectPath;

    @Value("${output.diagram.name}")
    String outputDiagramName;

    @Value("${depth}")
    int depth;

    @Autowired
    AnalyzeService analyzeService;
    @Autowired
    PlantumlDriver plantumlDriver;

    @Override
    public void run(String... args) {
        long startTime = System.currentTimeMillis();

        Path projectPath = Paths.get(this.projectPath);
        Path output = Paths.get(outputDiagramName);

        PackageDependencies packageDependencies = analyzeService.packageDependencies(new ProjectLocation(projectPath))
                .applyDepth(new PackageDepth(this.depth));

        LOGGER.info("関連数: " + packageDependencies.number().asText());

        showDepth(packageDependencies);

        plantumlDriver.output(packageDependencies, output);

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }

    private void showDepth(PackageDependencies outputRelation) {
        PackageDepth maxDepth = outputRelation.allPackages().maxDepth();

        LOGGER.info("最大深度: {}", maxDepth.value());
        for (PackageDepth depth : maxDepth.surfaceList()) {
            PackageDependencies dependencies = outputRelation.applyDepth(depth);
            LOGGER.info("深度 {} の関連数: {} ", depth.value(), dependencies.number().asText());
        }
    }
}

