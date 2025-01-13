package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public record ServiceMethods(List<ServiceMethod> list) {

    public static ServiceMethods from(JigTypes serviceJigTypes, MethodRelations methodRelations) {
        var list = serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().stream())
                .map(method -> ServiceMethod.from(method, methodRelations))
                .toList();
        return new ServiceMethods(list);
    }

    public boolean empty() {
        return list().isEmpty();
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list().stream()
                .anyMatch(serviceMethod -> serviceMethod.sameIdentifier(methodDeclaration));
    }
}
