package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.Method;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;
    private final RequestMappingAnnotations requestMappingAnnotations;

    public ControllerAngle(Method method, TypeAnnotations typeAnnotations) {
        this.method = method;
        this.requestMappingAnnotations = new RequestMappingAnnotations(method, typeAnnotations);
    }

    public Method method() {
        return method;
    }

    public RequestMappingAnnotations controllerAnnotations() {
        return requestMappingAnnotations;
    }
}
