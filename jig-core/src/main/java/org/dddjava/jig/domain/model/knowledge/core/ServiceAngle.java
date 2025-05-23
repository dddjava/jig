package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.members.instruction.BasicInstruction;
import org.dddjava.jig.domain.model.data.members.instruction.IfInstruction;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethod;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
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
    Collection<EntrypointMethod> entrypointMethods;

    Collection<MethodCall> usingServiceMethods;
    RepositoryMethods usingRepositoryMethods;

    private ServiceAngle(ServiceMethod serviceMethod, RepositoryMethods usingRepositoryMethods, Collection<MethodCall> usingServiceMethods, Collection<EntrypointMethod> entrypointMethods, Collection<JigMethodIdentifier> userServiceMethods) {
        this.serviceMethod = serviceMethod;

        this.usingRepositoryMethods = usingRepositoryMethods;
        this.usingServiceMethods = usingServiceMethods;

        this.entrypointMethods = entrypointMethods;
        this.userServiceMethods = userServiceMethods;
    }

    public static ServiceAngle from(ServiceMethods serviceMethods, Entrypoints entrypoints, DatasourceMethods datasourceMethods, ServiceMethod serviceMethod) {
        List<MethodCall> usingMethods = serviceMethod.usingMethods().methodCalls();

        Collection<JigMethodIdentifier> userServiceMethods = serviceMethod.callerMethods().filter(jigMethodIdentifier -> serviceMethods.contains(jigMethodIdentifier));
        Collection<MethodCall> usingServiceMethods = serviceMethod.usingMethods().invokedMethodStream()
                .filter(invokedMethod -> serviceMethods.contains(invokedMethod.jigMethodIdentifier()))
                .toList();
        RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
        Collection<EntrypointMethod> entrypointMethods = entrypoints.collectEntrypointMethodOf(serviceMethod.callerMethods());
        return new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, entrypointMethods, userServiceMethods);
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

    // TODO ミューテーションしても落ちなかったのでテストが必要
    public boolean useNull() {
        return serviceMethod.method().instructions().containsAny(instruction -> {
            if (instruction instanceof IfInstruction ifInstruction) {
                return ifInstruction.kind() == IfInstruction.Kind.NULL判定;
            }
            return instruction == BasicInstruction.NULL参照;
        });
    }

    public Collection<JigMethodIdentifier> userServiceMethods() {
        return userServiceMethods;
    }

    public boolean isNotPublicMethod() {
        return !serviceMethod.isPublic();
    }

    public Collection<MethodCall> usingServiceMethods() {
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
