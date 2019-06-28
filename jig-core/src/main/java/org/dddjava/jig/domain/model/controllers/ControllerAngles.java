package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.richmethod.RequestHandlerMethod;

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

    public ControllerAngles(ControllerMethods controllerMethods) {
        List<ControllerAngle> list = new ArrayList<>();
        for (RequestHandlerMethod method : controllerMethods.list()) {
            list.add(new ControllerAngle(method));
        }
        this.list = list;
    }
}
