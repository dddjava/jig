package jig.application.service;

import jig.domain.model.diagram.*;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.springframework.stereotype.Service;

@Service
public class DiagramService {

    DiagramMaker maker;
    DiagramConverter diagramConverter;

    public DiagramService(DiagramMaker maker, DiagramConverter diagramConverter) {
        this.maker = maker;
        this.diagramConverter = diagramConverter;
    }

    public Diagram generateFrom(PackageDependencies packageDependencies) {
        DiagramSource diagramSource = diagramConverter.toDiagramSource(packageDependencies);
        return maker.make(diagramSource);
    }
}
