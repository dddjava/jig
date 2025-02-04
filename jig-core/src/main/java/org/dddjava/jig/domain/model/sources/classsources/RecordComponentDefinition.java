package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

public record RecordComponentDefinition(
        String name,
        TypeIdentifier typeIdentifier
) {
}
