package jig.domain.model.relation;

import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.specification.Specification;

public enum RelationType {
    DEPENDENCY,
    FIELD,
    METHOD,
    METHOD_RETURN_TYPE,
    METHOD_PARAMETER,
    METHOD_USE_TYPE,
    IMPLEMENT,
    METHOD_USE_METHOD;

    public static void register(RelationRepository repository, Specification specification) {

        specification.fieldIdentifiers().list().forEach(repository::registerField);
        specification.constantIdentifiers().list().forEach(repository::registerConstants);

        specification.instanceMethodSpecifications().forEach(methodSpecification -> {
            MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;
            repository.registerMethod(methodDeclaration);
            repository.registerMethodParameter(methodDeclaration);
            repository.registerMethodReturnType(methodDeclaration, methodSpecification.getReturnTypeName());

            for (TypeIdentifier interfaceTypeIdentifier : specification.interfaceTypeIdentifiers.list()) {
                repository.registerImplementation(methodDeclaration, methodDeclaration.with(interfaceTypeIdentifier));
            }

            repository.registerMethodUseFields(methodDeclaration, methodSpecification.usingFields());

            methodSpecification.usingMethods.forEach(usingMethodIdentifier -> {
                repository.registerMethodUseMethod(methodDeclaration, usingMethodIdentifier);
                repository.registerMethodUseType(methodDeclaration, usingMethodIdentifier.declaringType());
            });
        });

        repository.registerDependency(specification.typeIdentifier(), specification.useTypes());
    }
}
