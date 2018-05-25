package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ImplementationMethods {
    List<ImplementationMethod> list;

    public ImplementationMethods(List<ImplementationMethod> list) {
        this.list = list;
    }

    public ImplementationMethods(Implementations implementations) {
        this(new ArrayList<>());

        for (Implementation implementation : implementations.list()) {
            for (MethodImplementation methodSpecification : implementation.instanceMethodSpecifications()) {
                MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;

                for (TypeIdentifier interfaceTypeIdentifier : implementation.interfaceTypeIdentifiers.list()) {
                    MethodDeclaration implMethod = methodDeclaration.with(interfaceTypeIdentifier);
                    list.add(new ImplementationMethod(methodDeclaration, implMethod));
                }
            }
        }

    }

    public ImplementationMethodStream stream() {
        return new ImplementationMethodStream(list.stream());
    }

    public static class ImplementationMethodStream {

        private final Stream<ImplementationMethod> stream;

        public ImplementationMethodStream(Stream<ImplementationMethod> stream) {
            this.stream = stream;
        }

        public ImplementationMethodStream filterInterfaceMethodIs(MethodDeclaration methodDeclaration) {
            return new ImplementationMethodStream(stream.filter(implementationMethod -> implementationMethod.interfaceMethodIs(methodDeclaration)));
        }

        public MethodDeclarations concrete() {
            return stream.map(ImplementationMethod::implementationMethod).collect(MethodDeclarations.collector());
        }
    }
}
