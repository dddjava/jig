package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.unit.method.Methods;

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

    public ServiceAngles(ServiceMethods serviceMethods,
                         MethodRelations methodRelations,
                         CharacterizedTypes characterizedTypes,
                         MethodUsingFields methodUsingFields,
                         Methods methods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            list.add(new ServiceAngle(serviceMethod, methodRelations, characterizedTypes, methodUsingFields, methods));
        }
        this.list = list;
    }

    public ServiceAngles filterReturnsBoolean() {
        List<ServiceAngle> collect = list.stream()
                .filter(serviceAngle -> serviceAngle.method().methodReturn().isBoolean())
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

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
