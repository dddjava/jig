package jig.application.service;

import jig.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiagramService {

    @Autowired
    DiagramRepository repository;
    @Autowired
    DiagramMaker maker;

    public DiagramIdentifier generateImmediately(DiagramSource source) {
        DiagramIdentifier identifier = repository.register(source);
        maker.make(identifier);
        return identifier;
    }

    public DiagramIdentifier request(DiagramSource source) {
        DiagramIdentifier identifier = repository.register(source);
        maker.makeAsync(identifier);
        return identifier;
    }

    public Diagram get(DiagramIdentifier identifier) {
        return repository.get(identifier);
    }
}
