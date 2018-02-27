package jig.model.diagram;

import java.util.Objects;

public class DiagramIdentifier {

    private final String identifier;

    public DiagramIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiagramIdentifier that = (DiagramIdentifier) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    public String getIdentifier() {
        return identifier;
    }
}
