package org.dddjava.jig.domain.model.jigmodel.presentations;

import org.dddjava.jig.domain.model.jigmodel.jigobject.member.RequestHandlerMethod;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;

import java.util.Comparator;
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
        return list.stream()
                .sorted(Comparator.comparing(requestHandlerMethod -> requestHandlerMethod.method().declaration().asFullNameText()))
                .collect(toList());
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
