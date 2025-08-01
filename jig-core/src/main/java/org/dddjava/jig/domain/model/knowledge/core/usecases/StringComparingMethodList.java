package org.dddjava.jig.domain.model.knowledge.core.usecases;

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
public class StringComparingMethodList {

    List<JigMethod> methods;

    StringComparingMethodList(List<JigMethod> methods) {
        this.methods = methods;
    }

    public static StringComparingMethodList createFrom(InputAdapters inputAdapters, ServiceMethods serviceMethods) {
        Stream<JigMethod> targetMethodStream = Stream.concat(
                inputAdapters.listRequestHandlerMethods().stream()
                        .map(entrypointMethod -> entrypointMethod.jigMethod()),
                serviceMethods.list().stream()
                        .map(serviceMethod -> serviceMethod.method())
        );
        return createFrom(targetMethodStream);
    }

    static StringComparingMethodList createFrom(Stream<JigMethod> target) {
        // String#equals(Object)
        JigMethodId jigMethodId = JigMethodId.from(
                TypeId.from(String.class),
                "equals",
                List.of(TypeId.from(Object.class))
        );
        return new StringComparingMethodList(target
                .filter(jigMethod -> jigMethod.isCall(jigMethodId))
                .toList());
    }

    public List<JigMethod> list() {
        return methods;
    }
}
