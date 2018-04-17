package jig.classlist;

import jig.application.service.AnalyzeService;
import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.jdeps.*;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
import jig.infrastructure.jdeps.JdepsExecutor;
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
import java.util.Collections;
import java.util.List;

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
    RelationAnalyzer relationAnalyzer;
    @Autowired
    AnalyzeService analyzeService;
    @Autowired
    DiagramService diagramService;

    @Override
    public void run(String... args) throws IOException {
        long startTime = System.currentTimeMillis();

        Path projectPath = Paths.get(this.projectPath);
        Path output = Paths.get(outputDiagramName);

        PackageDependencies packageDependencies = analyzeService.packageDependencies(new ProjectLocation(projectPath));

        PackageDependencies jdepsPackageDependencies = relationAnalyzer.analyzeRelations(new AnalysisCriteria(
                new SearchPaths(Collections.singletonList(projectPath)),
                new AnalysisClassesPattern(packagePattern + "\\..+"),
                new DependenciesPattern(packagePattern + "\\..+"),
                AnalysisTarget.PACKAGE));

        debugUntilRemoveJdeps(packageDependencies, jdepsPackageDependencies);

        PackageDependencies outputRelation = jdepsPackageDependencies
                // jdepsは関連のないパッケージを検出しないので、class解析で検出したパッケージで上書きする
                .withAllPackage(packageDependencies.allPackages())
                .applyDepth(new PackageDepth(this.depth));
        LOGGER.info("関連数: " + outputRelation.number().asText());

        Diagram diagram = diagramService.generateFrom(outputRelation);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
        LOGGER.info(output.toAbsolutePath() + "を出力しました。");

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * jdepsをなくせるまで、検証用に検出数の差を表示しておく
     *
     * @param packageDependencies
     * @param jdepsPackageDependencies
     */
    private void debugUntilRemoveJdeps(PackageDependencies packageDependencies, PackageDependencies jdepsPackageDependencies) {
        LOGGER.debug("件数       : " + packageDependencies.number().asText());
        LOGGER.debug("件数(jdeps): " + jdepsPackageDependencies.number().asText());

        List<PackageDependency> list = packageDependencies.list();
        List<PackageDependency> jdepsList = jdepsPackageDependencies.list();
        jdepsList.stream()
                .filter(relation -> !list.contains(relation))
                .forEach(relation -> LOGGER.debug("jdepsでのみ検出された依存: " + relation.from().value() + " -> " + relation.to().value()));
    }

    @Bean
    public DiagramConverter diagramConverter(PackageIdentifierFormatter formatter, JapaneseNameRepository repository) {
        return new PlantumlDiagramConverter(formatter, repository);
    }

    @Bean
    RelationAnalyzer relationAnalyzer() {
        return new JdepsExecutor();
    }
}

