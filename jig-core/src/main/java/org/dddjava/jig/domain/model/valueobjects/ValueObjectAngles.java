package org.dddjava.jig.domain.model.valueobjects;

import java.util.List;

public class ValueObjectAngles {

    List<ValueObjectAngle> list;

    public ValueObjectAngles(List<ValueObjectAngle> list) {
        this.list = list;
    }

    public List<ValueObjectAngle> list() {
        return list;
    }
}
