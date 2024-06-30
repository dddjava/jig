package org.dddjava.jig.domain.model.sources.additional;

import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;

public record AdditionalMethodModel(
        MethodIdentifier methodIdentifier,
        MethodAlias methodAlias,
        MethodDescription methodDescription
) {
}
