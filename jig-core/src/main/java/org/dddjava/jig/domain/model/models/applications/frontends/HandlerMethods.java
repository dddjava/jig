package org.dddjava.jig.domain.model.models.applications.frontends;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return new HandlerMethods(jigTypes.list().stream()
                .filter(requestHandlerType)
                .flatMap(HandlerMethods::collectHandlerMethod)
                .toList());
    }

    public static HandlerMethods from(JigType jigType) {
        if (requestHandlerType.test(jigType)
                // RabbitListenerはComponentに付与されている想定
                || jigType.hasAnnotation(new TypeIdentifier("org.springframework.stereotype.Component"))) {
            return new HandlerMethods(collectHandlerMethod(jigType).toList());
        }

        return new HandlerMethods(List.of());
    }

    static Stream<HandlerMethod> collectHandlerMethod(JigType jigType) {
        return jigType.instanceMember().instanceMethods().list()
                .stream()
                .map(jigMethod -> new HandlerMethod(jigType, jigMethod))
                .filter(HandlerMethod::valid);
    }

    private static final Predicate<JigType> requestHandlerType = jigType -> jigType.hasAnnotation(new TypeIdentifier("org.springframework.stereotype.Controller"))
            || jigType.hasAnnotation(new TypeIdentifier("org.springframework.web.bind.annotation.RestController"))
            || jigType.hasAnnotation(new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice"));

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

    public Set<TypeIdentifier> controllerTypeIdentifiers() {
        return list.stream()
                .map(handlerMethod -> handlerMethod.typeIdentifier())
                .collect(Collectors.toSet());
    }

    public HandlerMethods merge(HandlerMethod handlerMethod) {
        for (HandlerMethod method : list) {
            if (method.same(handlerMethod)) {
                return this;
            }
        }
        ArrayList<HandlerMethod> newList = new ArrayList<>(this.list);
        newList.add(handlerMethod);
        return new HandlerMethods(newList);
    }
}
