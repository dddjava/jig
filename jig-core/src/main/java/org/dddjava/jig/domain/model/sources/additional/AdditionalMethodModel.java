package org.dddjava.jig.domain.model.sources.additional;

import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;

public record AdditionalMethodModel(
        MethodIdentifier methodIdentifier,
        MethodAlias methodAlias,
        MethodDescription methodDescription
) {
}
