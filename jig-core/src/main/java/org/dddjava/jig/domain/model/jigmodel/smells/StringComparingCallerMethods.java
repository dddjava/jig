package org.dddjava.jig.domain.model.jigmodel.smells;

import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodReturn;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.applications.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文字列比較を行なっているメソッド
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public class StringComparingCallerMethods {

    List<Method> methods;

    public StringComparingCallerMethods(List<Method> methods) {
        this.methods = methods;
    }

    public static StringComparingCallerMethods from(AnalyzedImplementation analyzedImplementation, Architecture architecture) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ControllerMethods controllerMethods = new ControllerMethods(typeByteCodes, architecture);
        ServiceMethods serviceMethods = new ServiceMethods(typeByteCodes, architecture);
        DatasourceMethods datasourceMethods = new DatasourceMethods(typeByteCodes, architecture);

        // String#equals(Object)
        MethodDeclaration stringEqualsMethod = new MethodDeclaration(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        new Arguments(Collections.singletonList(new TypeIdentifier(Object.class)))),
                new MethodReturn(new TypeIdentifier(boolean.class))
        );

        List<Method> methods = Stream.concat(
                controllerMethods.list().stream()
                        .filter(controllerMethod -> controllerMethod.isCall(stringEqualsMethod))
                        .map(controllerMethod -> controllerMethod.method()),
                serviceMethods.list().stream()
                        .filter(serviceMethod -> serviceMethod.isCall(stringEqualsMethod))
                        .map(controllerMethod -> controllerMethod.method())
        ).collect(Collectors.toList());

        return new StringComparingCallerMethods(methods);
    }

    public List<Method> list() {
        return methods;
    }
}
