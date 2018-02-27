package jig.cli;

import jig.application.service.AnalyzeService;
import jig.application.service.DiagramService;
import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramSource;
import jig.domain.model.jdeps.*;
import jig.domain.model.thing.ThingFormatter;
import jig.domain.model.thing.Things;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
    @Value("${target.source}")
    String targetSource;
    @Value("${package.pattern}")
    String packagePattern;

    @Value("${output.diagram.name}")
    String outoutDiagramName;

    @Autowired
    AnalyzeService analyzeService;
    @Autowired
    DiagramService diagramService;

    @Override
    public void run(String... args) throws IOException {
        Path sourceRoot = Paths.get(targetSource);
        Path output = Paths.get(outoutDiagramName);

        Things things = analyzeService.toModels(
                new AnalysisCriteria(
                        new SearchPaths(Collections.singletonList(Paths.get(targetClass))),
                        new AnalysisClassesPattern(packagePattern + "\\..+"),
                        new DependenciesPattern(packagePattern + "\\..+"),
                        AnalysisTarget.PACKAGE));
        ThingFormatter thingFormatter = analyzeService.modelFormatter(sourceRoot);
        DiagramSource diagramSource = diagramService.toDiagramSource(things, thingFormatter);
        DiagramIdentifier identifier = diagramService.request(diagramSource);
        diagramService.generate(identifier);
        Diagram diagram = diagramService.get(identifier);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
    }
}

