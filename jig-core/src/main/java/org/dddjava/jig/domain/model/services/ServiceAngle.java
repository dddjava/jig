package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.datasources.DatasourceMethods;
import org.dddjava.jig.domain.model.datasources.RepositoryMethods;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.CallerMethods;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.MethodRelations;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.UsingFields;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.UsingMethods;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;

    ServiceMethods userServiceMethods;
    ControllerMethods userControllerMethods;

    UsingFields usingFields;
    RepositoryMethods usingRepositoryMethods;
    boolean useStream;
    private boolean isPublic;

    ServiceAngle(ServiceMethod serviceMethod, MethodRelations methodRelations, ControllerMethods controllerMethods, ServiceMethods serviceMethods, DatasourceMethods datasourceMethods) {
        this.methodDeclaration = serviceMethod.methodDeclaration();
        this.usingFields = serviceMethod.methodUsingFields();
        this.isPublic = serviceMethod.isPublic();

        UsingMethods usingMethods = serviceMethod.usingMethods();
        this.usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods.methodDeclarations());
        this.useStream = usingMethods.containsStream();

        CallerMethods callerMethods = methodRelations.callerMethodsOf(serviceMethod.methodDeclaration().identifier());
        this.userControllerMethods = controllerMethods.filter(callerMethods.methodDeclarations());
        this.userServiceMethods = serviceMethods.filter(callerMethods.methodDeclarations());
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public boolean usingFromController() {
        return !userControllerMethods.empty();
    }

    public UsingFields usingFields() {
        return usingFields;
    }

    public String usingRepositoryMethods() {
        return usingRepositoryMethods.asSimpleText();
    }

    public boolean useStream() {
        return useStream;
    }

    public MethodDeclarations userServiceMethods() {
        return userServiceMethods.list().stream().map(ServiceMethod::methodDeclaration).collect(MethodDeclarations.collector());
    }

    public MethodDeclarations userControllerMethods() {
        return userControllerMethods.list().stream().map(Method::declaration).collect(MethodDeclarations.collector());
    }

    public boolean isNotPublicMethod() {
        return !isPublic;
    }
}
