package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

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

    public MethodDeclarations usingMethodsOf(MethodDeclaration method) {
        return stream().filterFrom(method).toMethods();
    }

    public MethodDeclarations userMethodsOf(MethodIdentifier method) {
        return stream().filterTo(method).fromMethods();
    }

    public TypeIdentifiers userMethodDeclaringTypesOf(MethodDeclaration method) {
        return stream().filterTo(method).fromTypeIdentifiers();
    }

    public MethodRelationStream stream() {
        return new MethodRelationStream(list.stream());
    }

    public CallerMethods callerMethodsOf(MethodDeclaration declaration) {
        List<MethodDeclaration> callers = list.stream()
                .filter(methodRelation -> methodRelation.toIs(declaration))
                .map(MethodRelation::from)
                .collect(toList());
        return new CallerMethods(callers);
    }
}
