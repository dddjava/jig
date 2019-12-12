package org.dddjava.jig.domain.model.jigloaded.relation.method;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * メソッドの使用しているメソッド一覧
 */
public class MethodRelations {

    private final List<MethodRelation> list;

    public MethodRelations(TypeByteCodes typeByteCodes) {
        this.list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            for (MethodByteCode methodByteCode : typeByteCode.methodByteCodes()) {
                CallerMethod callerMethod = new CallerMethod(methodByteCode.methodDeclaration);
                for (MethodDeclaration usingMethod : methodByteCode.usingMethods().list()) {
                    list.add(new MethodRelation(callerMethod, new CalleeMethod(usingMethod)));
                }
            }
        }
    }

    public CallerMethods callerMethodsOf(CalleeMethod calleeMethod) {
        List<CallerMethod> callers = list.stream()
                .filter(methodRelation -> methodRelation.calleeMethodIs(calleeMethod))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }
}
