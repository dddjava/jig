package jig.application.service;

import jig.domain.model.dependency.ModelFormatter;
import jig.domain.model.dependency.Models;
import jig.domain.model.*;
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

    public DiagramSource toDiagramSource(Models models, ModelFormatter modelFormatter) {
        String text = getString(models, modelFormatter);
        return new DiagramSource(text);
    }

    private String getString(Models models, ModelFormatter modelFormatter) {
        return models.format(modelFormatter);
    }
}
