package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodReturn;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.interpret.relation.method.CalleeMethod;
import org.dddjava.jig.domain.model.interpret.relation.method.CallerMethod;
import org.dddjava.jig.domain.model.interpret.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.interpret.relation.method.MethodRelations;

import java.util.Collections;
import java.util.List;

/**
 * 文字列比較を行なっているメソッド
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public class StringComparingCallerMethods {

    CallerMethods equalsCallerMethods;

    public StringComparingCallerMethods(MethodRelations methodRelations) {
        // String#equals(Object)
        CalleeMethod calleeMethod = new CalleeMethod(new MethodDeclaration(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        new Arguments(Collections.singletonList(new TypeIdentifier(Object.class)))),
                new MethodReturn(new TypeIdentifier(boolean.class))
        ));
        this.equalsCallerMethods = methodRelations.callerMethodsOf(calleeMethod);
    }

    public List<CallerMethod> list() {
        return equalsCallerMethods.list();
    }
}
