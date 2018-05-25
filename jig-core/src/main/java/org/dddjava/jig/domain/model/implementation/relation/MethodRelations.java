package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;

import java.util.ArrayList;
import java.util.List;

public class MethodRelations {

    List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    public MethodRelations(ByteCodes byteCodes) {
        this(new ArrayList<>());

        for (ByteCode byteCode : byteCodes.list()) {
            for (MethodImplementation methodSpecification : byteCode.instanceMethodSpecifications()) {
                MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;
                for (MethodDeclaration usingMethod : methodSpecification.usingMethods().list()) {
                    list.add(new MethodRelation(methodDeclaration, usingMethod));
                }
            }
        }
    }

    public MethodRelationStream stream() {
        return new MethodRelationStream(list.stream());
    }
}
