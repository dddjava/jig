package org.dddjava.jig.domain.model.models.applications.services;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {

    List<JigType> serviceJigTypes;
    MethodRelations methodRelations;
    Predicate<JigMethod> methodFilter;

    private ServiceMethods(List<JigType> serviceJigTypes, MethodRelations methodRelations, Predicate<JigMethod> methodFilter) {
        this.serviceJigTypes = serviceJigTypes;
        this.methodRelations = methodRelations;
        this.methodFilter = methodFilter;
    }

    public static ServiceMethods from(JigTypes jigTypes, MethodRelations methodRelations) {
        List<JigType> serviceJigTypes = jigTypes
                .listMatches(jigType ->
                        jigType.hasAnnotation(new TypeIdentifier("org.springframework.stereotype.Service")));
        return new ServiceMethods(serviceJigTypes, methodRelations, jigMethod -> true);
    }

    public boolean empty() {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .noneMatch(methodFilter);
    }

    public List<ServiceMethod> list() {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .filter(methodFilter)
                .map(method -> new ServiceMethod(method, methodRelations))
                .collect(toList());
    }

    public List<JigType> listJigTypes() {
        return serviceJigTypes;
    }

    public MethodDeclarations toMethodDeclarations() {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .filter(methodFilter)
                .map(jigMethod -> jigMethod.declaration())
                .collect(MethodDeclarations.collector());
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream())
                .filter(methodFilter)
                .anyMatch(jigMethod -> methodDeclaration.sameIdentifier(jigMethod.declaration()));
    }
}
