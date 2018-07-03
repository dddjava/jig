package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * メソッドの使用しているメソッド一覧
 */
public class MethodRelations {

    private final List<MethodRelation> list;

    public MethodRelations(ByteCodes byteCodes) {
        List<MethodRelation> list = new ArrayList<>();

        for (ByteCode byteCode : byteCodes.list()) {
            for (MethodByteCode methodByteCode : byteCode.methodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (MethodDeclaration usingMethod : methodByteCode.usingMethods().list()) {
                    list.add(new MethodRelation(methodDeclaration, usingMethod));
                }
            }
        }
        this.list = list;
    }

    public MethodDeclarations userMethodsOf(MethodDeclaration method) {
        return stream().filterTo(method).fromMethods();
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
}
