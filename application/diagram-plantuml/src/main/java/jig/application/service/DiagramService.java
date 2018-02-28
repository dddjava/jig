package jig.application.service;

import jig.domain.model.diagram.*;
import jig.model.relation.Relations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class DiagramService {

    @Autowired
    DiagramRepository repository;
    @Autowired
    DiagramMaker maker;
    @Autowired
    DiagramConverter diagramConverter;

    public void generate(DiagramIdentifier identifier) {
        DiagramSource source = repository.getSource(identifier);
        Diagram diagram = maker.make(source);
        repository.register(identifier, diagram);
    }

    public DiagramIdentifier request(DiagramSource source) {
        return repository.registerSource(source);
    }

    @Async
    public CompletableFuture<DiagramIdentifier> generateAsync(DiagramIdentifier identifier) {
        generate(identifier);
        return CompletableFuture.completedFuture(identifier);
    }

    public Diagram get(DiagramIdentifier identifier) {
        return repository.get(identifier);
    }

    public DiagramSource toDiagramSource(Relations things) {
        return diagramConverter.toDiagramSource(things);
    }
}
