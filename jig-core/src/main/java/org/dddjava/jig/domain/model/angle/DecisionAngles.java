package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.characteristic.Layer;

import java.util.List;
import java.util.stream.Collectors;

public class DecisionAngles {

    List<DecisionAngle> list;

    public DecisionAngles(List<DecisionAngle> list) {
        this.list = list;
    }

    public List<DecisionAngle> listOnlyLayer() {
        return list.stream()
                .filter(decisionAngle -> decisionAngle.typeLayer() != Layer.OTHER)
                .collect(Collectors.toList());
    }
}
