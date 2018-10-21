package org.dddjava.jig.domain.model.businessrules.values;

import org.dddjava.jig.domain.model.networks.type.TypeRelations;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の切り口一覧
 */
public class ValueAngles {

    List<ValueAngle> list;

    public ValueAngles(ValueKind valueKind, ValueTypes valueTypes, TypeRelations typeRelations) {
        List<ValueAngle> list = new ArrayList<>();
        for (ValueType valueType : valueTypes.list()) {
            list.add(new ValueAngle(valueKind, typeRelations, valueType));
        }
        this.list = list;
    }

    public List<ValueAngle> list() {
        return list;
    }
}
