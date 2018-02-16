package jig.domain.model;

public class Diagram {
    private final DiagramIdentifier identifier;
    private final byte[] bytes;

    public Diagram(DiagramIdentifier identifier, byte[] bytes) {
        this.identifier = identifier;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean matches(DiagramIdentifier identifier) {
        return this.identifier.equals(identifier);
    }
}
