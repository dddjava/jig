package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.richmethod.Method;

/**
 * 判断の切り口
 */
public class DecisionAngle {

    Method method;

    public DecisionAngle(Method method) {
        this.method = method;
    }

    public Method method() {
        return method;
    }
}
