package jig.diagram.plantuml.diagram;

public class Diagram {
    private final byte[] bytes;

    public Diagram(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
