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

    public Diagram generateImmediately(DiagramSource source) {
        DiagramIdentifier identifier = maker.request(source);
        maker.make(identifier);
        return maker.get(identifier);
    }

    public DiagramIdentifier request(DiagramSource source) {
        return maker.request(source);
    }

    public Diagram get(DiagramIdentifier identifier) {
        // TODO 作成を別のタイミングで行う
        maker.make(identifier);
        return maker.get(identifier);
    }
}
