package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.progress.ProgressAngles;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;
    private final ProgressAngles progressAngles;
    private final ControllerAnnotations controllerAnnotations;

    public ControllerAngle(Method method, TypeAnnotations typeAnnotations, ProgressAngles progressAngles) {
        this.method = method;
        this.progressAngles = progressAngles;
        this.controllerAnnotations = new ControllerAnnotations(typeAnnotations, method.methodAnnotations());
    }

    public Method method() {
        return method;
    }

    public ControllerAnnotations controllerAnnotations() {
        return controllerAnnotations;
    }

    public String progress() {
        return progressAngles.progressOf(method.declaration());
    }
}
