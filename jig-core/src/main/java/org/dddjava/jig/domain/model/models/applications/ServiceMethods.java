package org.dddjava.jig.domain.model.models.applications;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {
    private final List<JigMethod> methods;

    private ServiceMethods(List<JigMethod> list) {
        this.methods = list;
    }

    public static ServiceMethods from(JigTypes jigTypes) {
        List<JigType> services = jigTypes
                .listMatches(jigType ->
                        // TODO Architecture.isService と重複。こちらに寄せたい。
                        jigType.hasAnnotation(new TypeIdentifier("org.springframework.stereotype.Service")));
        return new ServiceMethods(services.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .collect(toList()));
    }

    public boolean empty() {
        return methods.isEmpty();
    }

    public List<ServiceMethod> list() {
        return methods.stream()
                .map(ServiceMethod::new)
                .collect(toList());
    }

    public ServiceMethods filter(CallerMethods callerMethods) {
        return methods.stream()
                .filter(method -> callerMethods.contains(method.declaration()))
                .collect(collectingAndThen(toList(), ServiceMethods::new));
    }

    public ServiceMethods intersect(MethodDeclarations methodDeclarations) {
        return methods.stream()
                .filter(method -> methodDeclarations.contains(method.declaration()))
                .collect(collectingAndThen(toList(), ServiceMethods::new));
    }

    public MethodDeclarations toMethodDeclarations() {
        return methods.stream()
                .map(JigMethod::declaration)
                .collect(MethodDeclarations.collector());
    }
}
