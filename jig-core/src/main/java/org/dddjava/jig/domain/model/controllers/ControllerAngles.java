package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.unit.method.Method;
import org.dddjava.jig.domain.model.unit.method.Methods;

import java.util.ArrayList;
import java.util.List;

/**
 * コントローラーの切り口一覧
 */
public class ControllerAngles {

    List<ControllerAngle> list;

    public List<ControllerAngle> list() {
        return list;
    }

    public ControllerAngles(Methods controllerMethods, TypeAnnotations typeAnnotations) {
        List<ControllerAngle> list = new ArrayList<>();
        for (Method method : controllerMethods.list()) {
            list.add(new ControllerAngle(method, typeAnnotations.filter(method.declaration().identifier().declaringType())));
        }
        this.list = list;
    }
}
