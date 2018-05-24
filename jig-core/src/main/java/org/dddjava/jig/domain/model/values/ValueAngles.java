package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

public class ValueAngles {

    List<ValueAngle> list;

    public ValueAngles(List<ValueAngle> list) {
        this.list = list;
    }

    public static ValueAngles of(ValueKind valueKind, TypeIdentifiers typeIdentifiers, TypeDependencies allTypeDependencies) {
        List<ValueAngle> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            list.add(ValueAngle.of(valueKind, allTypeDependencies, typeIdentifier));
        }
        return new ValueAngles(list);
    }

    public List<ValueAngle> list() {
        return list;
    }
}
