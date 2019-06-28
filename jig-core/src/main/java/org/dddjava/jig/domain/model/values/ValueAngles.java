package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.fact.relation.class_.ClassRelations;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の切り口一覧
 */
public class ValueAngles {

    List<ValueAngle> list;

    public ValueAngles(ValueKind valueKind, ValueTypes valueTypes, ClassRelations classRelations) {
        List<ValueAngle> list = new ArrayList<>();
        for (ValueType valueType : valueTypes.list()) {
            list.add(new ValueAngle(valueKind, classRelations, valueType));
        }
        this.list = list;
    }

    public List<ValueAngle> list() {
        return list;
    }
}
