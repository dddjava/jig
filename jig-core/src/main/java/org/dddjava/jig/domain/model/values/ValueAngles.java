package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

public class ValueAngles {

    List<ValueAngle> list;

    public ValueAngles(List<ValueAngle> list) {
        this.list = list;
    }

    public static ValueAngles of(ValueKind valueKind, ValueAngleSource valueAngleSource) {
        List<ValueAngle> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : valueAngleSource.getTypeIdentifiers(valueKind).list()) {
            list.add(ValueAngle.of(valueKind, valueAngleSource.getAllTypeDependencies(), typeIdentifier));
        }
        return new ValueAngles(list);
    }

    public List<ValueAngle> list() {
        return list;
    }
}
