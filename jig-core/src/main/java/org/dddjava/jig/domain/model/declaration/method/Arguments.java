package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * メソッド引数
 */
public class Arguments {

    private final List<TypeIdentifier> argumentTypes;

    public Arguments(List<TypeIdentifier> argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    String argumentsAsText() {
        return argumentTypes().stream().map(TypeIdentifier::fullQualifiedName).collect(joining(", "));
    }

    private List<TypeIdentifier> argumentTypes() {
        return argumentTypes;
    }

    String argumentsAsSimpleText() {
        return argumentTypes().stream().map(TypeIdentifier::asSimpleText).collect(joining(", "));
    }

    public TypeIdentifiers typeIdentifiers() {
        return new TypeIdentifiers(argumentTypes);
    }
}
