package org.dddjava.jig.domain.model.parts.classes.method;

import org.dddjava.jig.domain.model.parts.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public static Arguments from(List<ParameterizedType> parameterizedTypes) {
        // TODO 引数の総称型対応
        return new Arguments(parameterizedTypes.stream().map(parameterizedType -> parameterizedType.typeIdentifier()).collect(Collectors.toList()));
    }

    String argumentsAsText() {
        return argumentTypes.stream().map(TypeIdentifier::fullQualifiedName).collect(joining(", "));
    }

    String argumentsAsSimpleText() {
        return argumentTypes.stream().map(TypeIdentifier::asSimpleText).collect(joining(", "));
    }

    public TypeIdentifiers typeIdentifiers() {
        return new TypeIdentifiers(argumentTypes);
    }

    public boolean isSame(Arguments other) {
        return argumentTypes.equals(other.argumentTypes);
    }

    public String packageAbbreviationText() {
        return argumentTypes.stream().map(TypeIdentifier::packageAbbreviationText).collect(joining(", "));
    }
}
