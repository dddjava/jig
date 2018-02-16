package jig.application.service;

import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramMaker;
import jig.domain.model.DiagramSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiagramService {

    @Autowired
    DiagramMaker maker;

    public Diagram generateImmediately(DiagramSource diagramSource) {
        DiagramIdentifier identifier = maker.request(diagramSource);
        maker.make(identifier);
        return maker.get(identifier);
    }
}
