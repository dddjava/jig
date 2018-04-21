package jig.diagram.plantuml;

import jig.diagram.plantuml.diagram.Diagram;
import jig.diagram.plantuml.diagram.DiagramMaker;
import jig.diagram.plantuml.diagramsource.DiagramSource;
import jig.diagram.plantuml.diagramsource.DiagramSourceWriter;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PlantumlDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlantumlDriver.class);

    final PackageIdentifierFormatter formatter;
    final JapaneseNameRepository repository;

    public PlantumlDriver(PackageIdentifierFormatter formatter, JapaneseNameRepository repository) {
        this.formatter = formatter;
        this.repository = repository;
    }

    public void output(PackageDependencies packageDependencies, Path outputPath) {
        DiagramSource diagramSource = toDiagramSource(packageDependencies);
        Diagram diagram = toDiagram(diagramSource);
        writerDiagram(diagram, outputPath);
    }

    public DiagramSource toDiagramSource(PackageDependencies packageDependencies) {
        DiagramSourceWriter diagramSourceWriter = new DiagramSourceWriter(formatter, repository);
        return diagramSourceWriter.toDiagramSource(packageDependencies);
    }

    public Diagram toDiagram(DiagramSource diagramSource) {
        DiagramMaker diagramMaker = new DiagramMaker();
        return diagramMaker.make(diagramSource);
    }

    public void writerDiagram(Diagram diagram, Path outputPath) {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputPath))) {
            outputStream.write(diagram.getBytes());
            LOGGER.info("{} に出力しました。", outputPath.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("ダイアグラムの出力に失敗しました。", e);
        }
    }
}
