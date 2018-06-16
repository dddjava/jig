package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * メソッドの使用しているメソッド一覧
 */
public class MethodRelations {

    List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    public MethodRelations(ByteCodes byteCodes) {
        this(new ArrayList<>());

        for (ByteCode byteCode : byteCodes.list()) {
            for (MethodByteCode methodByteCode : byteCode.methodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (MethodDeclaration usingMethod : methodByteCode.usingMethods().list()) {
                    list.add(new MethodRelation(methodDeclaration, usingMethod));
                }
            }
        }
    }

    public MethodRelationStream stream() {
        return new MethodRelationStream(list.stream());
    }
}
