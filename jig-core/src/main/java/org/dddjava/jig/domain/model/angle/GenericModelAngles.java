package org.dddjava.jig.domain.model.angle;

import java.util.List;

public class GenericModelAngles {

    List<GenericModelAngle> list;

    public GenericModelAngles(List<GenericModelAngle> list) {
        this.list = list;
    }

    public List<GenericModelAngle> list() {
        return list;
    }
}
