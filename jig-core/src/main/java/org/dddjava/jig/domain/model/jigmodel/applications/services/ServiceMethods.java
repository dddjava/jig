package org.dddjava.jig.domain.model.jigmodel.applications.services;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Method;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {
    private final List<Method> methods;

    public ServiceMethods(List<Method> list) {
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

    public TypeIdentifiers typeIdentifiers() {
        return methods.stream()
                .map(method -> method.declaration().declaringType())
                .sorted()
                .distinct()
                .collect(TypeIdentifiers.collector());
    }
}
