package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * インタフェースを実装しているメソッド
 */
public class ImplementationMethods {
    List<ImplementationMethod> list;

    public ImplementationMethods(List<ImplementationMethod> list) {
        this.list = list;
    }

    public ImplementationMethods(ByteCodes byteCodes) {
        this(new ArrayList<>());

        for (ByteCode byteCode : byteCodes.list()) {
            for (MethodByteCode methodByteCode : byteCode.instanceMethodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;

                for (TypeIdentifier interfaceTypeIdentifier : byteCode.interfaceTypeIdentifiers.list()) {
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
