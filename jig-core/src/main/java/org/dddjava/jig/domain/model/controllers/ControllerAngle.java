package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.method.Method;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;

    public ControllerAngle(Method method) {
        this.method = method;
    }

    public Method method() {
        return method;
    }
}
