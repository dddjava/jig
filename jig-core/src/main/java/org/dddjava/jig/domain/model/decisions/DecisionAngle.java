package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.architecture.Layer;
import org.dddjava.jig.domain.model.unit.method.Method;

/**
 * 判断の切り口
 */
public class DecisionAngle {

    Method method;
    Layer layer;

    public DecisionAngle(Method method, Layer layer) {
        this.method = method;
        this.layer = layer;
    }

    public Method method() {
        return method;
    }

    public Layer typeLayer() {
        return layer;
    }
}
