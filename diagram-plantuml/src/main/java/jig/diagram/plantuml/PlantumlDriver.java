package jig.diagram.plantuml;

import jig.diagram.plantuml.diagram.Diagram;
import jig.diagram.plantuml.diagram.DiagramMaker;
import jig.diagram.plantuml.diagramsource.DiagramSource;
import jig.diagram.plantuml.diagramsource.DiagramSourceWriter;
import jig.domain.basic.FileWriteFailureException;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependencyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class PlantumlDriver implements PackageDependencyWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlantumlDriver.class);

    final PackageIdentifierFormatter formatter;
    final JapaneseNameRepository repository;

    public PlantumlDriver(PackageIdentifierFormatter formatter, JapaneseNameRepository repository) {
        this.formatter = formatter;
        this.repository = repository;
    }

    @Override
    public void write(PackageDependencies packageDependencies, OutputStream outputStream) {
        try {
            DiagramSource diagramSource = toDiagramSource(packageDependencies);
            Diagram diagram = toDiagram(diagramSource);
            outputStream.write(diagram.getBytes());
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }

    public DiagramSource toDiagramSource(PackageDependencies packageDependencies) {
        DiagramSourceWriter diagramSourceWriter = new DiagramSourceWriter(formatter, repository);
        return diagramSourceWriter.toDiagramSource(packageDependencies);
    }

    public Diagram toDiagram(DiagramSource diagramSource) {
        DiagramMaker diagramMaker = new DiagramMaker();
        return diagramMaker.make(diagramSource);
    }
}
