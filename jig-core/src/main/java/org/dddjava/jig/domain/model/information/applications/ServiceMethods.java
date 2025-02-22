package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;

/**
 * サービスメソッド一覧
 */
public record ServiceMethods(List<ServiceMethod> list) {

    public static ServiceMethods from(JigTypes serviceJigTypes, CallerMethodsFactory callerMethodsFactory) {
        var list = serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceJigMethodStream())
                .map(method -> ServiceMethod.from(method, callerMethodsFactory))
                .toList();
        return new ServiceMethods(list);
    }

    public boolean empty() {
        return list().isEmpty();
    }

    public boolean contains(JigMethodIdentifier jigMethodIdentifier) {
        return list().stream()
                .anyMatch(serviceMethod -> serviceMethod.method().jigMethodIdentifier().equals(jigMethodIdentifier));
    }
}
