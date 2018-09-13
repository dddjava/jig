package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.Method;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;
    private final ControllerAnnotations controllerAnnotations;

    public ControllerAngle(Method method, TypeAnnotations typeAnnotations) {
        this.method = method;
        this.controllerAnnotations = new ControllerAnnotations(typeAnnotations, method.methodAnnotations());
    }

    public Method method() {
        return method;
    }

    public ControllerAnnotations controllerAnnotations() {
        return controllerAnnotations;
    }
}
