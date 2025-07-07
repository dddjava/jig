package org.dddjava.jig.domain.model.information.members;


import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メソッドが使用しているメソッド一覧
 */
public record UsingMethods(List<MethodCall> methodCalls) {

    static UsingMethods from(Instructions instructions) {
        return new UsingMethods(instructions.methodCallStream().toList());
    }

    public boolean containsStream() {
        return methodCalls.stream()
                .map(MethodCall::returnType)
                .anyMatch(TypeIdentifier::isStream);
    }

    public Stream<MethodCall> invokedMethodStream() {
        return methodCalls.stream();
    }

    public boolean contains(JigMethodId jigMethodId) {
        return methodCalls.stream()
                .anyMatch(invokedMethod -> invokedMethod.jigMethodIdentifierIs(jigMethodId));
    }

    public String asSimpleTextSorted() {
        return methodCalls.stream()
                .map(invokedMethod -> {
                    return invokedMethod.methodOwner().asSimpleText() + "." + invokedMethod.methodName() +
                            invokedMethod.argumentTypes().stream().map(TypeIdentifier::asSimpleText).collect(Collectors.joining(", ", "(", ")"));
                })
                .sorted() // 出力の安定のために名前順にしている
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
