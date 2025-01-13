package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.UsingFields;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethod;
import org.dddjava.jig.domain.model.knowledge.smell.MethodWorries;
import org.dddjava.jig.domain.model.knowledge.smell.MethodWorry;
import org.dddjava.jig.domain.model.information.outputs.RepositoryMethods;

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
        return serviceMethod.method().usingMethods().containsStream();
    }

    public boolean useNull() {
        return serviceMethod.method().useNull();
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
