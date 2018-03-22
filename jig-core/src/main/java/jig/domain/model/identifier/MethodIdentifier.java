package jig.domain.model.identifier;

import java.util.Objects;

public class MethodIdentifier {

    private final Identifier typeIdentifier;
    private final String methodName;
    private final Identifiers argumentTypeIdentifiers;

    private final String fullText;

    public MethodIdentifier(Identifier typeIdentifier, String methodName, Identifiers argumentTypeIdentifiers) {
        this.typeIdentifier = typeIdentifier;
        this.methodName = methodName;
        this.argumentTypeIdentifiers = argumentTypeIdentifiers;
        this.fullText = typeIdentifier.value() + "." + methodName + "(" + argumentTypeIdentifiers.asText() + ")";
    }

    public String asFullText() {
        return fullText;
    }

    public String asSimpleText() {
        return methodName + "(" + argumentTypeIdentifiers.asSimpleText() + ")";
    }

    public Identifier typeIdentifier() {
        return typeIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodIdentifier that = (MethodIdentifier) o;
        return Objects.equals(fullText, that.fullText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullText);
    }
}
