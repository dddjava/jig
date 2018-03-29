package jig.domain.model.identifier;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class MethodSignature {

    private final String methodName;
    List<TypeIdentifier> argumentTypeIdentifiers;

    public MethodSignature(String methodName, List<TypeIdentifier> argumentTypeIdentifiers) {
        this.methodName = methodName;
        this.argumentTypeIdentifiers = argumentTypeIdentifiers;
    }

    public String asText() {
        return methodName + "(" + argumentsAsText() + ")";
    }

    public String asSimpleText() {
        return methodName + "(" + argumentsAsSimpleText() + ")";
    }

    String argumentsAsText() {
        return argumentTypeIdentifiers.stream().map(TypeIdentifier::fullQualifiedName).collect(joining(","));
    }

    String argumentsAsSimpleText() {
        return argumentTypeIdentifiers.stream().map(TypeIdentifier::asSimpleText).collect(joining(","));
    }

    public List<TypeIdentifier> arguments() {
        return argumentTypeIdentifiers;
    }
}
