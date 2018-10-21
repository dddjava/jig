package org.dddjava.jig.domain.model.threelayer.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.bytecode.UsingFields;
import org.dddjava.jig.domain.model.angle.unit.method.Method;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;
    private final RequestHandler requestHandler;
    private final UsingFields usingFields;

    public ControllerAngle(Method method, TypeAnnotations typeAnnotations, MethodUsingFields methodUsingFields) {
        this.method = method;
        this.requestHandler = new RequestHandler(method, typeAnnotations);
        this.usingFields = methodUsingFields.usingFieldsOf(method.declaration());
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
