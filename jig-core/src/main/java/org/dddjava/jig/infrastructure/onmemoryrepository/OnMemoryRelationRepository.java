package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.relation.ImplementationMethod;
import org.dddjava.jig.domain.model.implementation.relation.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    final List<ImplementationMethod> methodImplementMethods = new ArrayList<>();

    @Override
    public void registerImplementation(MethodDeclaration implementationMethod, MethodDeclaration interfaceMethod) {
        methodImplementMethods.add(new ImplementationMethod(implementationMethod, interfaceMethod));
    }

    @Override
    public ImplementationMethods allImplementationMethods() {
        return new ImplementationMethods(methodImplementMethods);
    }
}
