package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

/**
 * 判断の切り口
 */
public class DecisionAngle {

    MethodDeclaration methodDeclaration;
    Characteristics typeCharacteristics;

    public DecisionAngle(MethodDeclaration methodDeclaration, Characteristics typeCharacteristics) {
        this.methodDeclaration = methodDeclaration;
        this.typeCharacteristics = typeCharacteristics;
    }

    public static DecisionAngle of(CharacterizedTypes characterizedTypes, MethodDeclaration methodDeclaration) {
        return new DecisionAngle(methodDeclaration, characterizedTypes.stream().pickup(methodDeclaration.declaringType()).characteristics());
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }

    public Layer typeLayer() {
        if (typeCharacteristics.has(Characteristic.CONTROLLER)) return Layer.PRESENTATION;

        if (typeCharacteristics.has(Characteristic.SERVICE)) return Layer.APPLICATION;

        if (typeCharacteristics.has(Characteristic.REPOSITORY)) return Layer.DATASOURCE;
        if (typeCharacteristics.has(Characteristic.DATASOURCE)) return Layer.DATASOURCE;

        return Layer.OTHER;
    }
}
