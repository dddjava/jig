package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.implementation.analyzed.architecture.Architecture;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {
    private final List<Method> methods;

    public ServiceMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
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
