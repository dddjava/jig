package org.dddjava.jig.domain.model.models.frontends;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;

import java.util.ArrayList;
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

    public static HandlerMethods from(JigTypes jigTypes) {
        List<HandlerMethod> list = new ArrayList<>();

        TypeIdentifier controller = new TypeIdentifier("org.springframework.stereotype.Controller");
        TypeIdentifier restController = new TypeIdentifier("org.springframework.web.bind.annotation.RestController");
        TypeIdentifier controllerAdvice = new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice");
        List<JigType> frontends = jigTypes.listMatches(jigType ->
                jigType.hasAnnotation(controller)
                        || jigType.hasAnnotation(restController)
                        || jigType.hasAnnotation(controllerAdvice));
        for (JigType jigType : frontends) {
            for (JigMethod jigMethod : jigType.instanceMember().instanceMethods().list()) {
                HandlerMethod handlerMethod = new HandlerMethod(jigMethod, new Annotations(jigType.listAnnotations()));
                if (handlerMethod.valid()) {
                    list.add(handlerMethod);
                }
            }
        }
        return new HandlerMethods(list);
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
