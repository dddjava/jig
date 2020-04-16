package org.dddjava.jig.domain.model.jigmodel.applications.services;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigloaded.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;
import org.dddjava.jig.domain.model.jigloader.MethodFactory;
import org.dddjava.jig.domain.model.jigmodel.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * サービスメソッド一覧
 */
public class ServiceMethods {
    private final List<Method> methods;

    public ServiceMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        TypeByteCodes applications = ApplicationLayer.APPLICATION.filter(typeByteCodes, architecture);

        this.methods = applications.list().stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .map(methodByteCode -> MethodFactory.createMethod(methodByteCode))
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

    public TypeIdentifiers typeIdentifiers() {
        return methods.stream()
                .map(method -> method.declaration().declaringType())
                .sorted()
                .distinct()
                .collect(TypeIdentifiers.collector());
    }
}
