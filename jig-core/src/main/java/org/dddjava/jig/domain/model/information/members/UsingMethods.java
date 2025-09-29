package org.dddjava.jig.domain.model.information.members;


import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * メソッドが使用しているメソッド一覧
 *
 * @param methodCalls 使用しているメソッドのリスト。登場順とするためListにしている。
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

    public boolean containsAny(Predicate<MethodCall> predicate) {
        return methodCalls.stream().anyMatch(predicate);
    }
}
