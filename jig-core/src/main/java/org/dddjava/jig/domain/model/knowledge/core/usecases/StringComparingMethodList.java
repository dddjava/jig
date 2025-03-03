package org.dddjava.jig.domain.model.knowledge.core.usecases;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * 文字列比較を行なっているメソッド
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public class StringComparingMethodList {

    List<JigMethod> methods;

    StringComparingMethodList(List<JigMethod> methods) {
        this.methods = methods;
    }

    public static StringComparingMethodList createFrom(Entrypoints entrypoints, ServiceMethods serviceMethods) {
        Stream<JigMethod> targetMethodStream = Stream.concat(
                entrypoints.listRequestHandlerMethods().stream()
                        .map(entrypointMethod -> entrypointMethod.jigMethod()),
                serviceMethods.list().stream()
                        .map(serviceMethod -> serviceMethod.method())
        );
        return createFrom(targetMethodStream);
    }

    static StringComparingMethodList createFrom(Stream<JigMethod> target) {
        // String#equals(Object)
        JigMethodIdentifier jigMethodIdentifier = JigMethodIdentifier.from(
                TypeIdentifier.from(String.class),
                "equals",
                List.of(TypeIdentifier.from(Object.class))
        );
        return new StringComparingMethodList(target
                .filter(jigMethod -> jigMethod.isCall(jigMethodIdentifier))
                .collect(toList()));
    }

    public List<JigMethod> list() {
        return methods;
    }
}
