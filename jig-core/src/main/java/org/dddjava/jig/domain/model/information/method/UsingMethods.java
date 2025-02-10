package org.dddjava.jig.domain.model.information.method;


import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * メソッドが使用しているメソッド一覧
 */
public record UsingMethods(List<InvokedMethod> invokedMethods) {

    static UsingMethods from(Instructions instructions) {
        return new UsingMethods(instructions.instructMethods());
    }

    public boolean containsStream() {
        return invokedMethods.stream()
                .map(InvokedMethod::returnType)
                .anyMatch(TypeIdentifier::isStream);
    }

    public MethodDeclarations methodDeclarations() {
        return new MethodDeclarations(methodDeclarationStream().toList());
    }

    public Stream<MethodDeclaration> methodDeclarationStream() {
        return invokedMethods.stream()
                .map(invokedMethod -> {
                    return new MethodDeclaration(invokedMethod.methodOwner(),
                            new MethodSignature(invokedMethod.methodName(), invokedMethod.argumentTypes().toArray(TypeIdentifier[]::new)),
                            new MethodReturn(new ParameterizedType(invokedMethod.returnType())));
                });
    }
}
