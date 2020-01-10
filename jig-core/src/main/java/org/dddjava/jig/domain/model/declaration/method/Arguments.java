package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.Collections;
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

    public static Arguments empty() {
        return new Arguments(Collections.emptyList());
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

    public boolean isSame(Arguments other) {
        return argumentTypes.equals(other.argumentTypes);
    }
}
