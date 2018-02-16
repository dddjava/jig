package jig.domain.model;

public interface DiagramMaker {

    DiagramIdentifier request(DiagramSource source);

    void make(DiagramIdentifier identifier);

    Diagram get(DiagramIdentifier identifier);
}
