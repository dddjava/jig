package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.method.Method;

/**
 * 判断の切り口
 */
public class DecisionAngle {

    Method method;
    Characteristics typeCharacteristics;

    public DecisionAngle(CharacterizedTypes characterizedTypes, Method method) {
        this.method = method;
        this.typeCharacteristics = characterizedTypes.stream().pickup(method.declaration().declaringType()).characteristics();
    }

    public Method method() {
        return method;
    }

    public Layer typeLayer() {
        if (typeCharacteristics.has(Characteristic.CONTROLLER)) return Layer.PRESENTATION;

        if (typeCharacteristics.has(Characteristic.SERVICE)) return Layer.APPLICATION;

        if (typeCharacteristics.has(Characteristic.REPOSITORY)) return Layer.DATASOURCE;
        if (typeCharacteristics.has(Characteristic.DATASOURCE)) return Layer.DATASOURCE;

        return Layer.OTHER;
    }
}
