package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.unit.method.Method;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceMethods {
    private final List<Method> methods;

    public ServiceMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<TypeByteCode> serviceByteCodes = typeByteCodes.list().stream()
                .filter(typeByteCode -> architecture.isService(typeByteCode.typeAnnotations()))
                .collect(Collectors.toList());

        this.methods = serviceByteCodes.stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .map(MethodByteCode::method)
                .collect(Collectors.toList());
    }

    public boolean empty() {
        return methods.isEmpty();
    }

    public List<ServiceMethod> list() {
        return methods.stream()
                .map(ServiceMethod::new)
                .collect(Collectors.toList());
    }
}
