package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import java.util.List;
import java.util.stream.Stream;

/**
 * 文字列比較を行なっているメソッド
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public record StringComparingMethodList(List<JigMethod> methods) {

    // String#equals(Object)
    private static final JigMethodId EQUALS = JigMethodId.from(
            TypeId.STRING,
            "equals",
            List.of(TypeId.OBJECT)
    );

    public static StringComparingMethodList from(InputAdapters inputAdapters, ServiceMethods serviceMethods) {
        Stream<JigMethod> targetMethodStream = Stream.concat(
                inputAdapters.listEntrypoint().stream()
                        .map(entrypointMethod -> entrypointMethod.jigMethod()),
                serviceMethods.list().stream()
                        .map(serviceMethod -> serviceMethod.method())
        );
        return from(targetMethodStream);
    }

    static StringComparingMethodList from(Stream<JigMethod> target) {
        return new StringComparingMethodList(target
                .filter(jigMethod -> jigMethod.isCall(EQUALS))
                .toList());
    }

    public List<JigMethod> list() {
        return methods;
    }
}
