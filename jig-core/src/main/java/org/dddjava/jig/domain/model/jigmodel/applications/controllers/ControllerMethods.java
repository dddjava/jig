package org.dddjava.jig.domain.model.jigmodel.applications.controllers;

import org.dddjava.jig.domain.model.jigloaded.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.jigloaded.richmethod.RequestHandlerMethod;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * コントローラーメソッド一覧
 */
public class ControllerMethods {
    List<RequestHandlerMethod> list;

    public ControllerMethods(List<RequestHandlerMethod> list) {
        this.list = list;
    }

    public List<RequestHandlerMethod> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public ControllerMethods filter(CallerMethods callerMethods) {
        return list.stream()
                .filter(requestHandlerMethod -> requestHandlerMethod.anyMatch(callerMethods))
                .collect(collectingAndThen(toList(), ControllerMethods::new));
    }
}
