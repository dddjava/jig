package org.dddjava.jig.domain.model.implementation.analyzed.networks.method;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;

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
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (MethodDeclaration usingMethod : methodByteCode.usingMethods().list()) {
                    list.add(new MethodRelation(methodDeclaration, usingMethod));
                }
            }
        }
    }

    public MethodDeclarations userMethodsOf(MethodIdentifier method) {
        return list.stream()
                .filter(methodRelation -> methodRelation.toIs(method))
                .map(MethodRelation::from)
                .collect(MethodDeclarations.collector());
    }

    public CallerMethods callerMethodsOf(MethodDeclaration declaration) {
        List<MethodDeclaration> callers = list.stream()
                .filter(methodRelation -> methodRelation.toIs(declaration))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }
}
