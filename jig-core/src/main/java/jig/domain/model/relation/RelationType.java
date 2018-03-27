package jig.domain.model.relation;

import jig.domain.model.identifier.TypeIdentifier;
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

        specification.fieldTypeIdentifiers().list().forEach(fieldTypeIdentifier ->
                repository.registerField(specification.typeIdentifier, fieldTypeIdentifier));

        specification.methodSpecifications.forEach(methodSpecification -> {
            repository.registerMethod(methodSpecification.identifier);
            repository.registerMethodParameter(methodSpecification.identifier);

            repository.registerMethodReturnType(methodSpecification.identifier, methodSpecification.getReturnTypeName());

            for (TypeIdentifier interfaceTypeIdentifier : specification.interfaceIdentifiers.list()) {
                repository.registerImplementation(methodSpecification.identifier, methodSpecification.methodIdentifierWith(interfaceTypeIdentifier));
            }

            methodSpecification.usingFieldTypeIdentifiers.forEach(fieldTypeName ->
                    repository.registerMethodUseType(methodSpecification.identifier, fieldTypeName));

            methodSpecification.usingMethodIdentifiers.forEach(methodName -> {
                repository.registerMethodUseMethod(methodSpecification.identifier, methodName);
                repository.registerMethodUseType(methodSpecification.identifier, methodName.typeIdentifier());
            });
        });
    }
}
