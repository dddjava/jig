package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.richmethod.Method;
import org.dddjava.jig.domain.model.richmethod.RequestHandlerMethod;
import org.dddjava.jig.domain.model.richmethod.UsingFields;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    RequestHandlerMethod requestHandlerMethod;

    public ControllerAngle(RequestHandlerMethod requestHandlerMethod) {
        this.requestHandlerMethod = requestHandlerMethod;
    }

    public Method method() {
        return requestHandlerMethod.method();
    }

    public RequestHandlerMethod requestHandler() {
        return requestHandlerMethod;
    }

    public UsingFields usingFields() {
        return method().usingFields();
    }
}
