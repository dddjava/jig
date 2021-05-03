package org.dddjava.jig.domain.model.models.applications;

import org.dddjava.jig.domain.model.models.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.backends.RepositoryMethods;
import org.dddjava.jig.domain.model.models.frontends.HandlerMethod;
import org.dddjava.jig.domain.model.models.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.MethodWorry;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.relation.method.UsingFields;
import org.dddjava.jig.domain.model.parts.relation.method.UsingMethods;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    ServiceMethod serviceMethod;

    ServiceMethods userServiceMethods;
    HandlerMethods userHandlerMethods;

    ServiceMethods usingServiceMethods;
    RepositoryMethods usingRepositoryMethods;

    ServiceAngle(ServiceMethod serviceMethod, MethodRelations methodRelations, HandlerMethods handlerMethods, ServiceMethods serviceMethods, DatasourceMethods datasourceMethods) {
        this.serviceMethod = serviceMethod;

        UsingMethods usingMethods = serviceMethod.usingMethods();
        this.usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods.methodDeclarations());
        this.usingServiceMethods = serviceMethods.intersect(usingMethods.methodDeclarations());

        CallerMethods callerMethods = methodRelations.callerMethodsOf(serviceMethod.methodDeclaration());
        this.userHandlerMethods = handlerMethods.filter(callerMethods);
        this.userServiceMethods = serviceMethods.filter(callerMethods);
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
        return userServiceMethods.list().stream().map(ServiceMethod::methodDeclaration).collect(MethodDeclarations.collector());
    }

    public MethodDeclarations userControllerMethods() {
        // TODO requestHandlerMethodsのようなのを返す。MethodDeclarationsは汎用的すぎる。
        return userHandlerMethods.list().stream()
                .map(HandlerMethod::method)
                .map(JigMethod::declaration)
                .collect(MethodDeclarations.collector());
    }

    public boolean isNotPublicMethod() {
        return !serviceMethod.isPublic();
    }

    public ServiceMethods usingServiceMethods() {
        return usingServiceMethods;
    }
}
