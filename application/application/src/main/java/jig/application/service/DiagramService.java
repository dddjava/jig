package jig.application.service;

import jig.model.diagram.*;
import jig.model.thing.ThingFormatter;
import jig.model.thing.Things;
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

    public DiagramSource toDiagramSource(Things things, ThingFormatter thingFormatter) {
        String text = getString(things, thingFormatter);
        return new DiagramSource(text);
    }

    private String getString(Things things, ThingFormatter thingFormatter) {
        return things.format(thingFormatter);
    }
}
