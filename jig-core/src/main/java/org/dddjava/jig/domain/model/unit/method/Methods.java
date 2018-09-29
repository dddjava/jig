package org.dddjava.jig.domain.model.unit.method;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * メソッド一覧
 */
public class Methods {
    List<Method> list;

    public Methods(List<Method> list) {
        this.list = list;
    }

    public MethodDeclarations declarations() {
        return list.stream().map(Method::declaration).collect(MethodDeclarations.collector());
    }

    public List<Method> list() {
        list.sort(Comparator.comparing(method -> method.declaration().asFullNameText()));
        return list;
    }

    public Methods filterHasDecision() {
        List<Method> list = this.list.stream().filter(Method::hasDecision).collect(toList());
        return new Methods(list);
    }

    public Methods controllerMethods(CharacterizedTypes characterizedTypes) {
        return new Methods(list.stream().filter(method -> method.isControllerMethod(characterizedTypes)).collect(Collectors.toList()));
    }
}
