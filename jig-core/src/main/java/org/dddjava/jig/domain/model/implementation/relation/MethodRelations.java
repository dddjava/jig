package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;

import java.util.ArrayList;
import java.util.List;

public class MethodRelations {

    List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    public MethodRelations(Implementations implementations) {
        this(new ArrayList<>());

        for (Implementation implementation : implementations.list()) {
            for (MethodImplementation methodSpecification : implementation.instanceMethodSpecifications()) {
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
