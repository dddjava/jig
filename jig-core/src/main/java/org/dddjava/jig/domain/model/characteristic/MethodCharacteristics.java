package org.dddjava.jig.domain.model.characteristic;

import java.util.Collection;

/**
 * メソッドの特徴一覧
 */
public class MethodCharacteristics {

    Collection<MethodCharacteristic> methodCharacteristics;

    public MethodCharacteristics(Collection<MethodCharacteristic> methodCharacteristics) {
        this.methodCharacteristics = methodCharacteristics;
    }

    public boolean isNotPublicMethod() {
        return !methodCharacteristics.contains(MethodCharacteristic.PUBLIC);
    }
}
