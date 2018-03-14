package jig.classlist;

import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.jdeps.*;
import jig.domain.model.relation.Relations;
import jig.infrastructure.OnMemoryJapanaseNameRepository;
import jig.infrastructure.javaparser.PackageInfoReader;
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
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = "jig")
public class PackageDiagramApplication implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(PackageDiagramApplication.class.getName());

    public static void main(String[] args) {
        System.setProperty("PLANTUML_LIMIT_SIZE", "65536");
        SpringApplication.run(PackageDiagramApplication.class, args);
    }

    @Value("${target.class}")
    String targetClass;

    @Value("${package.pattern}")
    String packagePattern;

    @Value("${output.diagram.name}")
    String outputDiagramName;

    @Autowired
    RelationAnalyzer relationAnalyzer;
    @Autowired
    DiagramService diagramService;

    @Override
    public void run(String... args) throws IOException {
        Path output = Paths.get(outputDiagramName);

        Relations relations = relationAnalyzer.analyzeRelations(new AnalysisCriteria(
                new SearchPaths(Collections.singletonList(Paths.get(targetClass))),
                new AnalysisClassesPattern(packagePattern + "\\..+"),
                new DependenciesPattern(packagePattern + "\\..+"),
                AnalysisTarget.PACKAGE));
        Diagram diagram = diagramService.generateFrom(relations);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
        logger.info(output.toAbsolutePath() + "を出力しました。");
    }

    @Bean
    public DiagramConverter diagramConverter(@Value("${package.pattern}") String packageNamePattern,
                                             @Value("${target.source}") String targetSource) {
        PlantumlNameFormatter nameFormatter = new PlantumlNameFormatter();
        nameFormatter.setNameShortenPattern(packageNamePattern + "\\.");

        PackageInfoReader packageInfoReader = new PackageInfoReader(Paths.get(targetSource));
        JapaneseNameRepository repository = new OnMemoryJapanaseNameRepository();
        packageInfoReader.registerTo(repository);

        return new PlantumlDiagramConverter(nameFormatter, repository);
    }

    @Bean
    RelationAnalyzer relationAnalyzer() {
        return new JdepsExecutor();
    }
}

