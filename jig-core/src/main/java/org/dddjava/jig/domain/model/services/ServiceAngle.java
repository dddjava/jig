package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.interpret.relation.method.CalleeMethod;
import org.dddjava.jig.domain.model.interpret.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.interpret.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.interpret.richmethod.*;
import org.dddjava.jig.domain.model.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.repositories.RepositoryMethods;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;

    ServiceMethods userServiceMethods;
    ServiceMethods usingServiceMethods;

    ControllerMethods userControllerMethods;

    UsingFields usingFields;
    RepositoryMethods usingRepositoryMethods;
    private boolean isPublic;
    MethodWorries methodWorries;

    ServiceAngle(ServiceMethod serviceMethod, MethodRelations methodRelations, ControllerMethods controllerMethods, ServiceMethods serviceMethods, DatasourceMethods datasourceMethods) {
        this.methodDeclaration = serviceMethod.methodDeclaration();
        this.usingFields = serviceMethod.methodUsingFields();
        this.isPublic = serviceMethod.isPublic();

        methodWorries = serviceMethod.methodWorries();

        UsingMethods usingMethods = serviceMethod.usingMethods();
        this.usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods.methodDeclarations());
        this.usingServiceMethods = serviceMethods.intersect(usingMethods.methodDeclarations());

        CallerMethods callerMethods = methodRelations.callerMethodsOf(new CalleeMethod(serviceMethod.methodDeclaration()));
        this.userControllerMethods = controllerMethods.filter(callerMethods);
        this.userServiceMethods = serviceMethods.filter(callerMethods);
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

    public RepositoryMethods usingRepositoryMethods() {
        return usingRepositoryMethods;
    }

    public boolean useStream() {
        return methodWorries.contains(MethodWorry.StreamAPIを使用している);
    }

    public boolean useNull() {
        return methodWorries.contains(MethodWorry.NULLリテラルを使用している, MethodWorry.NULL判定をしている);
    }


    public MethodDeclarations userServiceMethods() {
        return userServiceMethods.list().stream().map(ServiceMethod::methodDeclaration).collect(MethodDeclarations.collector());
    }

    public MethodDeclarations userControllerMethods() {
        // TODO requestHandlerMethodsのようなのを返す。MethodDeclarationsは汎用的すぎる。
        return userControllerMethods.list().stream()
                .map(RequestHandlerMethod::method)
                .map(Method::declaration)
                .collect(MethodDeclarations.collector());
    }

    public boolean isNotPublicMethod() {
        return !isPublic;
    }

    public ServiceMethods usingServiceMethods() {
        return usingServiceMethods;
    }
}
