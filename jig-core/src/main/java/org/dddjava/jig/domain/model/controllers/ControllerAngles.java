package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.declaration.method.Methods;

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

    public ControllerAngles(Methods handlerMethods) {
        List<ControllerAngle> list = new ArrayList<>();
        for (Method method : handlerMethods.list()) {
            list.add(new ControllerAngle(method));
        }
        this.list = list;
    }
}
