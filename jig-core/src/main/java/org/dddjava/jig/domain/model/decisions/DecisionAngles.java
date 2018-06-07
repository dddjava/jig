package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判断の切り口一覧
 */
public class DecisionAngles {

    List<DecisionAngle> list;

    public DecisionAngles(List<DecisionAngle> list) {
        this.list = list;
    }

    public static DecisionAngles of(MethodDeclarations methods, CharacterizedTypes characterizedTypes) {
        List<DecisionAngle> list = new ArrayList<>();
        for (MethodDeclaration methodDeclaration : methods.list()) {
            list.add(DecisionAngle.of(characterizedTypes, methodDeclaration));
        }
        return new DecisionAngles(list);
    }

    public List<DecisionAngle> listOnlyLayer() {
        return list.stream()
                .filter(decisionAngle -> decisionAngle.typeLayer() != Layer.OTHER)
                .collect(Collectors.toList());
    }
}
