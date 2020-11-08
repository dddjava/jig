package org.dddjava.jig.domain.model.jigmodel.services;

import org.dddjava.jig.domain.model.jigmodel.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.MethodWorries;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.MethodWorry;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.RequestHandlerMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.UsingFields;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.UsingMethods;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.jigmodel.repositories.RepositoryMethods;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;

    ServiceMethods userServiceMethods;
    ControllerMethods userControllerMethods;

    UsingFields usingFields;
    ServiceMethods usingServiceMethods;
    RepositoryMethods usingRepositoryMethods;

    boolean isPublic;
    MethodWorries methodWorries;
    ServiceMethod serviceMethod;

    ServiceAngle(ServiceMethod serviceMethod, MethodRelations methodRelations, ControllerMethods controllerMethods, ServiceMethods serviceMethods, DatasourceMethods datasourceMethods) {
        this.serviceMethod = serviceMethod;
        this.methodDeclaration = serviceMethod.methodDeclaration();
        this.usingFields = serviceMethod.methodUsingFields();
        this.isPublic = serviceMethod.isPublic();

        methodWorries = serviceMethod.methodWorries();

        UsingMethods usingMethods = serviceMethod.usingMethods();
        this.usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods.methodDeclarations());
        this.usingServiceMethods = serviceMethods.intersect(usingMethods.methodDeclarations());

        CallerMethods callerMethods = methodRelations.callerMethodsOf(serviceMethod.methodDeclaration());
        this.userControllerMethods = controllerMethods.filter(callerMethods);
        this.userServiceMethods = serviceMethods.filter(callerMethods);
    }

    public ServiceMethod serviceMethod() {
        return serviceMethod;
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
                .map(JigMethod::declaration)
                .collect(MethodDeclarations.collector());
    }

    public boolean isNotPublicMethod() {
        return !isPublic;
    }

    public ServiceMethods usingServiceMethods() {
        return usingServiceMethods;
    }
}
