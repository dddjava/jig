package jig.domain.model;

public interface DiagramRepository {

    DiagramIdentifier register(DiagramSource source);

    Diagram get(DiagramIdentifier identifier);
}
