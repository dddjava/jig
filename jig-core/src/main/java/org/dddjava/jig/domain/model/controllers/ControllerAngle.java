package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.unit.method.Method;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;
    private final RequestHandler requestHandler;

    public ControllerAngle(Method method, TypeAnnotations typeAnnotations) {
        this.method = method;
        this.requestHandler = new RequestHandler(method, typeAnnotations);
    }

    public Method method() {
        return method;
    }

    public RequestHandler requestHandler() {
        return requestHandler;
    }
}
