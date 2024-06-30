package org.dddjava.jig.domain.model.sources.additional;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.List;

public record AdditionalTypeModel(
        TypeIdentifier typeIdentifier,
        List<String> imports,
        TypeAlias typeAlias,
        TypeDescription typeDescription,
        List<AdditionalMethodModel> methods
) {
}