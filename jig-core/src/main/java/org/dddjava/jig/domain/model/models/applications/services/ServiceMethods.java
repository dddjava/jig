package org.dddjava.jig.domain.model.models.applications.services;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {

    List<JigType> serviceJigTypes;
    MethodRelations methodRelations;

    private ServiceMethods(List<JigType> serviceJigTypes, MethodRelations methodRelations) {
        this.serviceJigTypes = serviceJigTypes;
        this.methodRelations = methodRelations;
    }

    public static ServiceMethods from(JigTypes jigTypes, MethodRelations methodRelations) {
        List<JigType> serviceJigTypes = jigTypes
                .listMatches(jigType ->
                        jigType.hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Service")));
        return new ServiceMethods(serviceJigTypes, methodRelations);
    }

    public boolean empty() {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().stream())
                .findAny()
                .isEmpty();
    }

    public List<ServiceMethod> list() {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().stream())
                .map(method -> new ServiceMethod(method, methodRelations))
                .collect(toList());
    }

    public List<JigType> listJigTypes() {
        return serviceJigTypes;
    }

    public MethodDeclarations toMethodDeclarations() {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().stream())
                .map(jigMethod -> jigMethod.declaration())
                .collect(MethodDeclarations.collector());
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return serviceJigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().stream())
                .anyMatch(jigMethod -> methodDeclaration.sameIdentifier(jigMethod.declaration()));
    }

    public Optional<JigMethod> find(MethodDeclaration usingMethod) {
        return serviceJigTypes.stream()
                .filter(jigType -> jigType.identifier().equals(usingMethod.declaringType()))
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().stream())
                .filter(jigMethod -> jigMethod.declaration().sameIdentifier(usingMethod))
                .findAny();
    }
}
