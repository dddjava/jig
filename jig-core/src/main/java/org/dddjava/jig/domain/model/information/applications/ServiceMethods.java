package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {

    JigTypes serviceJigTypes;
    MethodRelations methodRelations;

    public ServiceMethods(JigTypes serviceJigTypes, MethodRelations methodRelations) {
        this.serviceJigTypes = serviceJigTypes;
        this.methodRelations = methodRelations;
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
        return serviceJigTypes.list();
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
