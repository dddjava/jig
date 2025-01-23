package org.dddjava.jig.domain.model.knowledge.core.usecases;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;

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
        MethodDeclaration stringEqualsMethod = new MethodDeclaration(
                TypeIdentifier.from(String.class),
                new MethodSignature("equals", TypeIdentifier.from(Object.class)),
                MethodReturn.fromTypeOnly(TypeIdentifier.from(boolean.class))
        );

        List<JigMethod> methods = Stream.concat(
                entrypoint.listRequestHandlerMethods().stream()
                        .filter(entrypointMethod -> entrypointMethod.isCall(stringEqualsMethod))
                        .map(entrypointMethod -> entrypointMethod.method()),
                serviceMethods.list().stream()
                        .filter(serviceMethod -> serviceMethod.isCall(stringEqualsMethod))
                        .map(serviceMethod -> serviceMethod.method())
        ).collect(toList());

        return new StringComparingMethodList(methods);
    }

    public List<JigMethod> list() {
        return methods;
    }
}
