package jig.domain.model;

public interface DiagramRepository {

    DiagramIdentifier register(DiagramSource source);

    void register(Diagram source);

    Diagram get(DiagramIdentifier identifier);

    DiagramSource getSource(DiagramIdentifier identifier);
}
