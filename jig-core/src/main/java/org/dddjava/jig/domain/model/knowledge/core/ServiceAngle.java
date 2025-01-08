package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.models.applications.inputs.EntrypointMethod;
import org.dddjava.jig.domain.model.models.applications.outputs.RepositoryMethods;
import org.dddjava.jig.domain.model.models.applications.usecases.ServiceMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.MethodWorry;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.UsingFields;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    ServiceMethod serviceMethod;

    MethodDeclarations userServiceMethods;
    List<EntrypointMethod> entrypointMethods;

    MethodDeclarations usingServiceMethods;
    RepositoryMethods usingRepositoryMethods;

    ServiceAngle(ServiceMethod serviceMethod, RepositoryMethods usingRepositoryMethods, MethodDeclarations usingServiceMethods, List<EntrypointMethod> entrypointMethods, MethodDeclarations userServiceMethods) {
        this.serviceMethod = serviceMethod;

        this.usingRepositoryMethods = usingRepositoryMethods;
        this.usingServiceMethods = usingServiceMethods;

        this.entrypointMethods = entrypointMethods;
        this.userServiceMethods = userServiceMethods;
    }

    public ServiceMethod serviceMethod() {
        return serviceMethod;
    }

    public MethodDeclaration method() {
        return serviceMethod.methodDeclaration();
    }

    public boolean usingFromController() {
        return !entrypointMethods.isEmpty();
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

    public boolean isNotPublicMethod() {
        return !serviceMethod.isPublic();
    }

    public MethodDeclarations usingServiceMethods() {
        return usingServiceMethods;
    }

    public Set<TypeIdentifier> userControllerTypeIdentifiers() {
        return entrypointMethods.stream()
                .map(entrypointMethod -> entrypointMethod.typeIdentifier())
                .collect(Collectors.toSet());
    }
}
