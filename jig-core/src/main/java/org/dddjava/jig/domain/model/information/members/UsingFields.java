package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * メソッドが使用しているフィールド
 */
public class UsingFields {
    private final Collection<JigFieldIdentifier> fieldIds;

    private UsingFields(Collection<JigFieldIdentifier> fieldIds) {
        this.fieldIds = fieldIds;
    }

    static UsingFields from(Instructions instructions) {
        return new UsingFields(instructions.fieldReferences());
    }

    public String typeNames() {
        return fieldIds.stream()
                .map(JigFieldIdentifier::declaringTypeIdentifier)
                .map(TypeIdentifier::asSimpleText)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
