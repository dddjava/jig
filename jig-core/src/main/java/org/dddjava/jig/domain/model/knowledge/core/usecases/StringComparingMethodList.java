package org.dddjava.jig.domain.model.knowledge.core.usecases;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
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

    public static StringComparingMethodList createFrom(Entrypoint entrypoint, ServiceMethods serviceMethods) {
        // String#equals(Object)
        JigMethodIdentifier jigMethodIdentifier = JigMethodIdentifier.from(
                TypeIdentifier.from(String.class),
                "equals",
                List.of(TypeIdentifier.from(Object.class))
        );

        List<JigMethod> methods = Stream.concat(
                entrypoint.listRequestHandlerMethods().stream()
                        .filter(entrypointMethod -> entrypointMethod.isCall(jigMethodIdentifier))
                        .map(entrypointMethod -> entrypointMethod.jigMethod()),
                serviceMethods.list().stream()
                        .filter(serviceMethod -> serviceMethod.isCall(jigMethodIdentifier))
                        .map(serviceMethod -> serviceMethod.method())
        ).collect(toList());

        return new StringComparingMethodList(methods);
    }

    public List<JigMethod> list() {
        return methods;
    }
}
