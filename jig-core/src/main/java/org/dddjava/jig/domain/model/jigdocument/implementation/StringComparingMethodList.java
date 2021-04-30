package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigmodel.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethods;
import org.dddjava.jig.domain.model.parts.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.method.MethodReturn;
import org.dddjava.jig.domain.model.parts.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;

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

    public static StringComparingMethodList createFrom(ControllerMethods controllerMethods, ServiceMethods serviceMethods) {
        // String#equals(Object)
        MethodDeclaration stringEqualsMethod = new MethodDeclaration(
                TypeIdentifier.of(String.class),
                new MethodSignature("equals", TypeIdentifier.of(Object.class)),
                new MethodReturn(TypeIdentifier.of(boolean.class))
        );

        List<JigMethod> methods = Stream.concat(
                controllerMethods.list().stream()
                        .filter(controllerMethod -> controllerMethod.isCall(stringEqualsMethod))
                        .map(controllerMethod -> controllerMethod.method()),
                serviceMethods.list().stream()
                        .filter(serviceMethod -> serviceMethod.isCall(stringEqualsMethod))
                        .map(controllerMethod -> controllerMethod.method())
        ).collect(toList());

        return new StringComparingMethodList(methods);
    }

    public List<JigMethod> list() {
        return methods;
    }
}
