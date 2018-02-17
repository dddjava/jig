package jig.domain.model;

public interface DiagramMaker {

    void make(DiagramIdentifier identifier);

    void makeAsync(DiagramIdentifier identifier);
}
