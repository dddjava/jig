package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.List;

/**
 * サービスメソッド一覧
 */
public record ServiceMethods(List<ServiceMethod> list) {

    public static ServiceMethods from(JigTypes serviceJigTypes, CallerMethodsFactory callerMethodsFactory) {
        var list = serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().stream())
                .map(method -> ServiceMethod.from(method, callerMethodsFactory))
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
