package jig.classlist;

import jig.application.service.AnalyzeService;
import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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

    @Value("${package.pattern}")
    String packagePattern;

    @Value("${output.diagram.name}")
    String outputDiagramName;

    @Value("${depth}")
    int depth;

    @Autowired
    AnalyzeService analyzeService;
    @Autowired
    DiagramService diagramService;

    @Override
    public void run(String... args) throws IOException {
        long startTime = System.currentTimeMillis();

        Path projectPath = Paths.get(this.projectPath);
        Path output = Paths.get(outputDiagramName);

        PackageDependencies packageDependencies = analyzeService.packageDependencies(new ProjectLocation(projectPath))
                .applyDepth(new PackageDepth(this.depth));

        LOGGER.info("関連数: " + packageDependencies.number().asText());

        showDepth(packageDependencies);

        Diagram diagram = diagramService.generateFrom(packageDependencies);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
        LOGGER.info(output.toAbsolutePath() + "を出力しました。");

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

    @Bean
    public DiagramConverter diagramConverter(PackageIdentifierFormatter formatter, JapaneseNameRepository repository) {
        return new PlantumlDiagramConverter(formatter, repository);
    }
}

