package org.dddjava.jig.domain.model.characteristic;

import java.util.List;

public class ValueTypes {
    List<ValueType> list;

    public ValueTypes(List<ValueType> list) {
        this.list = list;
    }

    public boolean contains(ValueType valueType) {
        return list.contains(valueType);
    }
}
