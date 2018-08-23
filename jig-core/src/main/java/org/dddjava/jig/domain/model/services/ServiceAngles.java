package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.progress.ProgressAngles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    private ServiceAngles(List<ServiceAngle> list) {
        this.list = list;
    }

    public List<ServiceAngle> list() {
        return list;
    }

    public ServiceAngles(MethodDeclarations serviceMethods,
                         MethodRelations methodRelations,
                         CharacterizedTypes characterizedTypes,
                         MethodUsingFields methodUsingFields,
                         CharacterizedMethods characterizedMethods,
                         ProgressAngles progressAngles) {
        List<ServiceAngle> list = new ArrayList<>();
        for (MethodDeclaration serviceMethod : serviceMethods.list()) {
            list.add(new ServiceAngle(serviceMethod, methodRelations, characterizedTypes, methodUsingFields, characterizedMethods, progressAngles));
        }
        this.list = list;
    }

    public ServiceAngles filterReturnsBoolean() {
        List<ServiceAngle> collect = list.stream()
                .filter(serviceAngle -> serviceAngle.method().returnType().isBoolean())
                .collect(Collectors.toList());
        return new ServiceAngles(collect);
    }

    public MethodDeclarations userServiceMethods() {
        return list.stream()
                .flatMap(serviceAngle -> serviceAngle.userServiceMethods().list().stream())
                .distinct()
                .collect(MethodDeclarations.collector());
    }

    public MethodDeclarations userControllerMethods() {
        return list.stream()
                .flatMap(serviceAngle -> serviceAngle.userControllerMethods().list().stream())
                .distinct()
                .collect(MethodDeclarations.collector());
    }

    public boolean notContains(MethodDeclaration methodDeclaration) {
        return list.stream()
                .noneMatch(serviceAngle -> serviceAngle.method().sameIdentifier(methodDeclaration));
    }
}
