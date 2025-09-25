package org.dddjava.jig.domain.model.information.members;


import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * メソッドが使用しているメソッド一覧
 */
public record UsingMethods(List<MethodCall> methodCalls) {

    static UsingMethods from(Instructions instructions) {
        return new UsingMethods(instructions.methodCallStream().toList());
    }

    public boolean containsStreamAPI() {
        return methodCalls.stream()
                .map(MethodCall::returnType)
                .anyMatch(TypeId::isStreamAPI);
    }

    public Stream<MethodCall> invokedMethodStream() {
        return methodCalls.stream();
    }

    public boolean contains(JigMethodId jigMethodId) {
        return methodCalls.stream()
                .anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(jigMethodId));
    }

    // FIXME テストでのみ使用しているテキスト出力。廃止したい。
    public String asSimpleTextSorted() {
        return methodCalls.stream()
                .map(invokedMethod -> {
                    return invokedMethod.methodOwner().asSimpleText() + "." + invokedMethod.methodName() +
                            invokedMethod.argumentTypes().stream().map(TypeId::asSimpleText).collect(joining(", ", "(", ")"));
                })
                .sorted() // 出力の安定のために名前順にしている
                .collect(joining(", ", "[", "]"));
    }

    public boolean containsAny(Predicate<MethodCall> predicate) {
        return methodCalls.stream().anyMatch(predicate);
    }
}
