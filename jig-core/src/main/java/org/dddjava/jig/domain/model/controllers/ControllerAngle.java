package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.UsingFields;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;
    private final RequestHandler requestHandler;
    private final UsingFields usingFields;

    public ControllerAngle(Method method, TypeAnnotations typeAnnotations) {
        this.method = method;
        this.requestHandler = new RequestHandler(method, typeAnnotations);
        this.usingFields = method.usingFields();
    }

    public Method method() {
        return method;
    }

    public RequestHandler requestHandler() {
        return requestHandler;
    }

    public UsingFields usingFields() {
        return usingFields;
    }
}
