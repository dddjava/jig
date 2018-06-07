package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.relation.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の切り口一覧
 */
public class ValueAngles {

    List<ValueAngle> list;

    public ValueAngles(List<ValueAngle> list) {
        this.list = list;
    }

    public static ValueAngles of(ValueKind valueKind, ValueTypes valueTypes, TypeDependencies typeDependencies) {
        List<ValueAngle> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : valueTypes.extract(valueKind).list()) {
            list.add(ValueAngle.of(valueKind, typeDependencies, typeIdentifier));
        }
        return new ValueAngles(list);
    }

    public List<ValueAngle> list() {
        return list;
    }
}
