package org.dddjava.jig.domain.model.information.members;


import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;
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

    public Stream<InvokedMethod> invokedMethodStream() {
        return invokedMethods.stream();
    }

    public boolean contains(JigMethodIdentifier jigMethodIdentifier) {
        return invokedMethods.stream()
                .anyMatch(invokedMethod -> invokedMethod.jigMethodIdentifierIs(jigMethodIdentifier));
    }

    public String asSimpleTextSorted() {
        return invokedMethods.stream()
                .map(invokedMethod -> {
                    return invokedMethod.methodOwner().asSimpleText() + "." + invokedMethod.methodName() +
                            invokedMethod.argumentTypes().stream().map(TypeIdentifier::asSimpleText).collect(Collectors.joining(", ", "(", ")"));
                })
                .sorted() // 出力の安定のために名前順にしている
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
