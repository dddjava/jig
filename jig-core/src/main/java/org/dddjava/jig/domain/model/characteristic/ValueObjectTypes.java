package org.dddjava.jig.domain.model.characteristic;

import java.util.List;

public class ValueObjectTypes {
    List<ValueObjectType> list;

    public ValueObjectTypes(List<ValueObjectType> list) {
        this.list = list;
    }

    public boolean contains(ValueObjectType valueObjectType) {
        return list.contains(valueObjectType);
    }
}
