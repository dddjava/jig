package jig.domain.model.identifier;

import java.util.Objects;

public class MethodIdentifier {

    private final TypeIdentifier typeIdentifier;
    private final String methodName;
    private final Identifiers argumentTypeIdentifiers;

    private final String fullText;

    public MethodIdentifier(TypeIdentifier typeIdentifier, String methodName, Identifiers argumentTypeIdentifiers) {
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

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public Identifiers argumentTypeIdentifiers() {
        return argumentTypeIdentifiers;
    }

    public MethodIdentifier with(TypeIdentifier typeIdentifier) {
        return new MethodIdentifier(typeIdentifier, this.methodName, this.argumentTypeIdentifiers);
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
