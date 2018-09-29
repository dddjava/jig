package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.unit.method.Method;
import org.dddjava.jig.domain.model.unit.method.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判断の切り口一覧
 */
public class DecisionAngles {

    List<DecisionAngle> list;

    public DecisionAngles(Methods methods, CharacterizedTypes characterizedTypes) {
        list = new ArrayList<>();
        for (Method method : methods.list()) {
            list.add(new DecisionAngle(characterizedTypes, method));
        }
    }

    public List<DecisionAngle> filter(Layer layer) {
        return list.stream()
                .filter(decisionAngle -> decisionAngle.typeLayer() == layer)
                .collect(Collectors.toList());
    }
}
