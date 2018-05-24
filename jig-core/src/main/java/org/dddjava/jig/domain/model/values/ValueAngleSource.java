package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.TypeDependencies;

public class ValueAngleSource {
    private final ValueTypes valueTypes;
    private final TypeDependencies allTypeDependencies;

    public ValueAngleSource(ValueTypes valueTypes, TypeDependencies allTypeDependencies) {
        this.valueTypes = valueTypes;
        this.allTypeDependencies = allTypeDependencies;
    }

    public TypeIdentifiers getTypeIdentifiers(ValueKind valueKind) {
        return valueTypes.extract(valueKind);
    }

    public TypeDependencies getAllTypeDependencies() {
        return allTypeDependencies;
    }
}
