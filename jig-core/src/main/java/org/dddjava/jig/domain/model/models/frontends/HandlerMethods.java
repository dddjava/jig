package org.dddjava.jig.domain.model.models.frontends;

import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * ハンドラ一覧
 */
public class HandlerMethods {
    List<HandlerMethod> list;

    public HandlerMethods(List<HandlerMethod> list) {
        this.list = list;
    }

    public List<HandlerMethod> list() {
        return list.stream()
                .sorted(Comparator.comparing(requestHandlerMethod -> requestHandlerMethod.method().declaration().asFullNameText()))
                .collect(toList());
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public HandlerMethods filter(CallerMethods callerMethods) {
        return list.stream()
                .filter(requestHandlerMethod -> requestHandlerMethod.anyMatch(callerMethods))
                .collect(collectingAndThen(toList(), HandlerMethods::new));
    }
}
