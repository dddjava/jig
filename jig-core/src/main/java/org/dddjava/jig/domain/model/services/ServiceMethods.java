package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ProjectData;
import org.dddjava.jig.domain.model.unit.method.Method;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class ServiceMethods {
    private final List<Method> methods;

    public ServiceMethods(ProjectData typeByteCodes, Architecture architecture) {
        List<TypeByteCode> serviceByteCodes = typeByteCodes.list().stream()
                .filter(typeByteCode -> architecture.isService(typeByteCode.typeAnnotations()))
                .collect(toList());

        this.methods = serviceByteCodes.stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .map(methodByteCode -> new Method(methodByteCode))
                .collect(toList());
    }

    private ServiceMethods(List<Method> list) {
        this.methods = list;
    }

    public boolean empty() {
        return methods.isEmpty();
    }

    public List<ServiceMethod> list() {
        return methods.stream()
                .map(ServiceMethod::new)
                .collect(toList());
    }

    public ServiceMethods filter(MethodDeclarations methodDeclarations) {
        return methods.stream()
                .filter(method -> methodDeclarations.contains(method.declaration()))
                .collect(collectingAndThen(toList(), ServiceMethods::new));
    }
}
