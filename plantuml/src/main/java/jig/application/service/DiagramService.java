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

    public Diagram generateImmediately(DiagramSource source) {
        DiagramIdentifier identifier = repository.register(source);
        maker.make(identifier);
        return repository.get(identifier);
    }

    public DiagramIdentifier request(DiagramSource source) {
        return repository.register(source);
    }

    public Diagram get(DiagramIdentifier identifier) {
        // TODO 作成を別のタイミングで行う
        maker.make(identifier);
        return repository.get(identifier);
    }
}
