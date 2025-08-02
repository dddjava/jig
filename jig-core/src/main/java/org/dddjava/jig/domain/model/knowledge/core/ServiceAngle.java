package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.members.instruction.IfInstruction;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.instruction.SimpleInstruction;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.outputs.RepositoryMethods;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    ServiceMethod serviceMethod;

    Collection<JigMethodId> userServiceMethods;
    Collection<Entrypoint> entrypoints;

    Collection<MethodCall> usingServiceMethods;
    RepositoryMethods usingRepositoryMethods;

    private ServiceAngle(ServiceMethod serviceMethod, RepositoryMethods usingRepositoryMethods, Collection<MethodCall> usingServiceMethods, Collection<Entrypoint> entrypoints, Collection<JigMethodId> userServiceMethods) {
        this.serviceMethod = serviceMethod;

        this.usingRepositoryMethods = usingRepositoryMethods;
        this.usingServiceMethods = usingServiceMethods;

        this.entrypoints = entrypoints;
        this.userServiceMethods = userServiceMethods;
    }

    public static ServiceAngle from(ServiceMethod serviceMethod, ServiceMethods serviceMethods, InputAdapters inputAdapters, DatasourceMethods datasourceMethods) {
        UsingMethods usingMethods = serviceMethod.usingMethods();

        Collection<JigMethodId> userServiceMethods = serviceMethod.callerMethods().filter(jigMethodId -> serviceMethods.contains(jigMethodId));
        Collection<MethodCall> usingServiceMethods = serviceMethod.usingMethods().invokedMethodStream()
                .filter(invokedMethod -> serviceMethods.contains(invokedMethod.jigMethodId()))
                .toList();
        RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
        Collection<Entrypoint> entrypointMethods = inputAdapters.collectEntrypointMethodOf(serviceMethod.callerMethods());
        return new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, entrypointMethods, userServiceMethods);
    }

    public ServiceMethod serviceMethod() {
        return serviceMethod;
    }

    public boolean usingFromController() {
        return !entrypoints.isEmpty();
    }

    public UsingFields usingFields() {
        return serviceMethod.methodUsingFields();
    }

    public RepositoryMethods usingRepositoryMethods() {
        return usingRepositoryMethods;
    }

    public boolean useStream() {
        return serviceMethod.method().usingMethods().containsStreamAPI();
    }

    // TODO ミューテーションしても落ちなかったのでテストが必要
    public boolean useNull() {
        return serviceMethod.method().instructions().containsAny(instruction -> {
            if (instruction instanceof IfInstruction ifInstruction) {
                return ifInstruction.kind() == IfInstruction.Kind.NULL判定;
            }
            return instruction == SimpleInstruction.NULL参照;
        });
    }

    public Collection<JigMethodId> userServiceMethods() {
        return userServiceMethods;
    }

    public boolean isNotPublicMethod() {
        return !serviceMethod.isPublic();
    }

    public Collection<MethodCall> usingServiceMethods() {
        return usingServiceMethods;
    }

    public Set<TypeId> userControllerTypeIds() {
        return entrypoints.stream()
                .map(entrypointMethod -> entrypointMethod.typeId())
                .collect(Collectors.toSet());
    }

    public JigMethodId jigMethodId() {
        return serviceMethod().method().jigMethodId();
    }

    public TypeId declaringType() {
        return serviceMethod.declaringType();
    }
}
