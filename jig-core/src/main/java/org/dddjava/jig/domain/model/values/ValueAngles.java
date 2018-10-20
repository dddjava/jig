package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の切り口一覧
 */
public class ValueAngles {

    List<ValueAngle> list;

    public ValueAngles(ValueKind valueKind, ValueTypes valueTypes, TypeDependencies typeDependencies) {
        List<ValueAngle> list = new ArrayList<>();
        for (ValueType valueType : valueTypes.list()) {
            list.add(new ValueAngle(valueKind, typeDependencies, valueType));
        }
        this.list = list;
    }

    public List<ValueAngle> list() {
        return list;
    }
}
