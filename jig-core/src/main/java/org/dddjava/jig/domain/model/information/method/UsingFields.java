package org.dddjava.jig.domain.model.information.method;

import org.dddjava.jig.domain.model.data.classes.method.instruction.FieldReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * メソッドが使用しているフィールド
 */
public class UsingFields {
    private final Collection<FieldReference> fieldReferences;

    UsingFields(Collection<FieldReference> fieldReferences) {
        this.fieldReferences = fieldReferences;
    }

    public String typeNames() {
        return fieldReferences.stream()
                .map(FieldReference::fieldTypeIdentifier)
                .map(TypeIdentifier::asSimpleText)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
