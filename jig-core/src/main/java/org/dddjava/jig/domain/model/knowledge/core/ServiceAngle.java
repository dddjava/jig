package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.members.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethod;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.outputs.RepositoryMethods;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    ServiceMethod serviceMethod;

    Collection<JigMethodIdentifier> userServiceMethods;
    List<EntrypointMethod> entrypointMethods;

    Collection<InvokedMethod> usingServiceMethods;
    RepositoryMethods usingRepositoryMethods;

    ServiceAngle(ServiceMethod serviceMethod, RepositoryMethods usingRepositoryMethods, Collection<InvokedMethod> usingServiceMethods, List<EntrypointMethod> entrypointMethods, Collection<JigMethodIdentifier> userServiceMethods) {
        this.serviceMethod = serviceMethod;

        this.usingRepositoryMethods = usingRepositoryMethods;
        this.usingServiceMethods = usingServiceMethods;

        this.entrypointMethods = entrypointMethods;
        this.userServiceMethods = userServiceMethods;
    }

    public ServiceMethod serviceMethod() {
        return serviceMethod;
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

    public Collection<JigMethodIdentifier> userServiceMethods() {
        return userServiceMethods;
    }

    public boolean isNotPublicMethod() {
        return !serviceMethod.isPublic();
    }

    public Collection<InvokedMethod> usingServiceMethods() {
        return usingServiceMethods;
    }

    public Set<TypeIdentifier> userControllerTypeIdentifiers() {
        return entrypointMethods.stream()
                .map(entrypointMethod -> entrypointMethod.typeIdentifier())
                .collect(Collectors.toSet());
    }

    public JigMethodIdentifier jigMethodIdentifier() {
        return serviceMethod().method().jigMethodIdentifier();
    }

    public TypeIdentifier declaringType() {
        return serviceMethod.declaringType();
    }
}
