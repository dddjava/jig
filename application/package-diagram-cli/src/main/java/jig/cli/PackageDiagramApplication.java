package jig.cli;

import jig.application.service.DiagramService;
import jig.domain.model.diagram.Diagram;
import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.diagram.DiagramIdentifier;
import jig.domain.model.diagram.DiagramSource;
import jig.infrastructure.javaparser.PackageInfoLibrary;
import jig.infrastructure.jdeps.JdepsExecutor;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import jig.infrastructure.plantuml.PlantumlNameFormatter;
import jig.model.jdeps.*;
import jig.model.relation.Relations;
import jig.model.tag.JapaneseNameDictionaryLibrary;
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

@SpringBootApplication(scanBasePackages = "jig")
public class PackageDiagramApplication implements CommandLineRunner {

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
        DiagramSource diagramSource = diagramService.toDiagramSource(relations);
        DiagramIdentifier identifier = diagramService.request(diagramSource);
        diagramService.generate(identifier);
        Diagram diagram = diagramService.get(identifier);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
    }

    @Bean
    public DiagramConverter diagramConverter(@Value("${package.pattern}") String packageNamePattern,
                                             @Value("${target.source}") String targetSource) {
        Path sourceRoot = Paths.get(targetSource);
        JapaneseNameDictionaryLibrary library = new PackageInfoLibrary(sourceRoot);
        PlantumlNameFormatter nameFormatter = new PlantumlNameFormatter(library.borrow());
        nameFormatter.setNameShortenPattern(packageNamePattern + "\\.");
        return new PlantumlDiagramConverter(nameFormatter);
    }

    @Bean
    RelationAnalyzer relationAnalyzer() {
        return new JdepsExecutor();
    }
}

