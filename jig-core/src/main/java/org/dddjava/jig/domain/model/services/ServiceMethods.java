package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.unit.method.Method;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceMethods {
    private final List<Method> methods;

    public ServiceMethods(List<Method> methods) {
        this.methods = methods;
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
