package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

public record RecordComponentDefinition(
        String name,
        TypeIdentifier typeIdentifier
) {
}
