package org.dddjava.jig.domain.model.models.applications;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 *
 * TODO Servicesにリネームしたい
 */
public class ServiceMethods {

    List<JigType> jigTypes;
    Predicate<JigMethod> methodFilter;

    private ServiceMethods(List<JigType> jigTypes, Predicate<JigMethod> methodFilter) {
        this.jigTypes = jigTypes;
        this.methodFilter = methodFilter;
    }

    public static ServiceMethods from(JigTypes jigTypes) {
        List<JigType> services = jigTypes
                .listMatches(jigType ->
                        jigType.hasAnnotation(new TypeIdentifier("org.springframework.stereotype.Service")));
        return new ServiceMethods(services, jigMethod -> true);
    }

    public boolean empty() {
        return jigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .noneMatch(methodFilter);
    }

    public List<ServiceMethod> list() {
        return jigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .filter(methodFilter)
                .map(ServiceMethod::new)
                .collect(toList());
    }

    public List<JigType> listJigTypes() {
        return jigTypes;
    }

    public ServiceMethods filter(CallerMethods callerMethods) {
        return new ServiceMethods(jigTypes, jigMethod -> callerMethods.contains(jigMethod.declaration()));
    }

    public ServiceMethods intersect(MethodDeclarations methodDeclarations) {
        return new ServiceMethods(jigTypes, jigMethod -> methodDeclarations.contains(jigMethod.declaration()));
    }

    public MethodDeclarations toMethodDeclarations() {
        return jigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .filter(methodFilter)
                .map(jigMethod -> jigMethod.declaration())
                .collect(MethodDeclarations.collector());
    }
}
