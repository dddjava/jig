package jig.domain.model;

import java.util.Objects;
import java.util.UUID;

public class DiagramIdentifier {

    private final String identifier;

    public DiagramIdentifier() {
        this.identifier = UUID.randomUUID().toString();
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
}
