package jig.classlist;

import jig.application.service.AnalyzeService;
import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.jdeps.*;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.dependency.Depth;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
import jig.infrastructure.jdeps.JdepsExecutor;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import jig.infrastructure.plantuml.PlantumlNameFormatter;
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
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = "jig")
public class PackageDiagramApplication implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(PackageDiagramApplication.class.getName());

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
        Path output = Paths.get(outputDiagramName);

        PackageDependencies packageDependencies = analyzeService.dependenciesIn(new ProjectLocation(Paths.get(projectPath)));

        PackageDependencies jdepsPackageDependencies = relationAnalyzer.analyzeRelations(new AnalysisCriteria(
                new SearchPaths(Collections.singletonList(Paths.get(projectPath))),
                new AnalysisClassesPattern(packagePattern + "\\..+"),
                new DependenciesPattern(packagePattern + "\\..+"),
                AnalysisTarget.PACKAGE));

        List<PackageDependency> list = packageDependencies.list();
        List<PackageDependency> jdepsList = jdepsPackageDependencies.list();
        logger.info("件数       : " + list.size());
        logger.info("件数(jdeps): " + jdepsList.size());
        jdepsList.stream()
                .filter(relation -> !list.contains(relation))
                .forEach(relation -> logger.info("jdepsでのみ検出された依存: " + relation.from().value() + " -> " + relation.to().value()));

        Depth depth = new Depth(this.depth);
        PackageDependencies outputRelation = jdepsPackageDependencies.applyDepth(depth);
        logger.info("出力件数: " + outputRelation.list().size());

        Diagram diagram = diagramService.generateFrom(outputRelation);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
        logger.info(output.toAbsolutePath() + "を出力しました。");
    }

    @Bean
    public DiagramConverter diagramConverter(@Value("${package.pattern}") String packageNamePattern,
                                             JapaneseNameRepository repository) {
        PlantumlNameFormatter nameFormatter = new PlantumlNameFormatter();
        nameFormatter.setNameShortenPattern(packageNamePattern + "\\.");

        analyzeService.readJavadoc(new ProjectLocation(Paths.get(projectPath)));

        return new PlantumlDiagramConverter(nameFormatter, repository);
    }

    @Bean
    RelationAnalyzer relationAnalyzer() {
        return new JdepsExecutor();
    }
}

