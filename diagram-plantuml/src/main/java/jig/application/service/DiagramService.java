package jig.application.service;

import jig.domain.model.diagram.*;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.springframework.stereotype.Service;

@Service
public class DiagramService {

    DiagramRepository repository;
    DiagramMaker maker;
    DiagramConverter diagramConverter;

    public DiagramService(DiagramRepository repository, DiagramMaker maker, DiagramConverter diagramConverter) {
        this.repository = repository;
        this.maker = maker;
        this.diagramConverter = diagramConverter;
    }

    public Diagram generateFrom(PackageDependencies packageDependencies) {
        DiagramSource diagramSource = diagramConverter.toDiagramSource(packageDependencies);
        DiagramIdentifier identifier = repository.registerSource(diagramSource);
        DiagramSource source = repository.getSource(identifier);
        Diagram diagram = maker.make(source);
        repository.register(identifier, diagram);
        return repository.get(identifier);
    }
}
