package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

public record RecordComponentDefinition(
        String name,
        TypeIdentifier typeIdentifier
) {
}
