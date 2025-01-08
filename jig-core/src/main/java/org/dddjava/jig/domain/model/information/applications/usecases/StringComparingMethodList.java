package org.dddjava.jig.domain.model.information.applications.usecases;

import org.dddjava.jig.domain.model.information.applications.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.parts.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

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
                new MethodReturn(TypeIdentifier.from(boolean.class))
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
