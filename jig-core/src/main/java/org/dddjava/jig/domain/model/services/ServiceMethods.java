package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.CallerMethods;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {
    private final List<Method> methods;

    public ServiceMethods(TypeByteCodes typeByteCodes) {
        TypeByteCodes applications = ApplicationLayer.APPLICATION.filter(typeByteCodes);

        this.methods = applications.list().stream()
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

    public ServiceMethods filter(CallerMethods callerMethods) {
        return methods.stream()
                .filter(method -> callerMethods.contains(method.declaration()))
                .collect(collectingAndThen(toList(), ServiceMethods::new));
    }

    public ServiceMethods intersect(MethodDeclarations methodDeclarations) {
        return methods.stream()
                .filter(method -> methodDeclarations.contains(method.declaration()))
                .collect(collectingAndThen(toList(), ServiceMethods::new));
    }

    public String reportText() {
        return methods.stream()
                .map(Method::declaration)
                .collect(MethodDeclarations.collector())
                .asSimpleText();
    }
}
