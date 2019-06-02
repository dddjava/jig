package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.architecture.ArchitectureBlock;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;

/**
 * 判断の切り口
 */
public class DecisionAngle {

    Method method;
    ArchitectureBlock architectureBlock;

    public DecisionAngle(Method method, ArchitectureBlock architectureBlock) {
        this.method = method;
        this.architectureBlock = architectureBlock;
    }

    public Method method() {
        return method;
    }

    public ArchitectureBlock typeLayer() {
        return architectureBlock;
    }
}
