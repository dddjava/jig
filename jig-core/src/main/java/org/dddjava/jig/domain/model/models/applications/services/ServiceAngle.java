package org.dddjava.jig.domain.model.models.applications.services;

import org.dddjava.jig.domain.model.models.applications.backends.RepositoryMethods;
import org.dddjava.jig.domain.model.models.applications.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.models.jigobject.member.MethodWorry;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.UsingFields;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    ServiceMethod serviceMethod;

    MethodDeclarations userServiceMethods;
    HandlerMethods userHandlerMethods;

    MethodDeclarations usingServiceMethods;
    RepositoryMethods usingRepositoryMethods;

    ServiceAngle(ServiceMethod serviceMethod, RepositoryMethods usingRepositoryMethods, MethodDeclarations usingServiceMethods, HandlerMethods userHandlerMethods, MethodDeclarations userServiceMethods) {
        this.serviceMethod = serviceMethod;

        this.usingRepositoryMethods = usingRepositoryMethods;
        this.usingServiceMethods = usingServiceMethods;

        this.userHandlerMethods = userHandlerMethods;
        this.userServiceMethods = userServiceMethods;
    }

    public ServiceMethod serviceMethod() {
        return serviceMethod;
    }

    public MethodDeclaration method() {
        return serviceMethod.methodDeclaration();
    }

    public boolean usingFromController() {
        return !userHandlerMethods.empty();
    }

    public UsingFields usingFields() {
        return serviceMethod.methodUsingFields();
    }

    public RepositoryMethods usingRepositoryMethods() {
        return usingRepositoryMethods;
    }

    public boolean useStream() {
        return serviceMethod.methodWorries().contains(MethodWorry.StreamAPIを使用している);
    }

    public boolean useNull() {
        return serviceMethod.methodWorries().contains(MethodWorry.NULLリテラルを使用している, MethodWorry.NULL判定をしている);
    }


    public MethodDeclarations userServiceMethods() {
        return userServiceMethods;
    }

    public HandlerMethods userControllerMethods() {
        return userHandlerMethods;
    }

    public boolean isNotPublicMethod() {
        return !serviceMethod.isPublic();
    }

    public MethodDeclarations usingServiceMethods() {
        return usingServiceMethods;
    }
}
