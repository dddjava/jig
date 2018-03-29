package jig.domain.model.relation;

import jig.domain.model.identifier.method.MethodIdentifier;
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

        specification.fieldTypeIdentifiers().list().forEach(fieldTypeIdentifier ->
                repository.registerField(specification.typeIdentifier, fieldTypeIdentifier));

        specification.methodSpecifications.forEach(methodSpecification -> {
            MethodIdentifier methodIdentifier = methodSpecification.identifier;
            repository.registerMethod(methodIdentifier);
            repository.registerMethodParameter(methodIdentifier);
            repository.registerMethodReturnType(methodIdentifier, methodSpecification.getReturnTypeName());

            for (TypeIdentifier interfaceTypeIdentifier : specification.interfaceTypeIdentifiers.list()) {
                repository.registerImplementation(methodIdentifier, methodIdentifier.with(interfaceTypeIdentifier));
            }

            methodSpecification.usingFieldTypeIdentifiers.forEach(fieldTypeIdentifier ->
                    repository.registerMethodUseType(methodIdentifier, fieldTypeIdentifier));

            methodSpecification.usingMethodIdentifiers.forEach(usingMethodIdentifier -> {
                repository.registerMethodUseMethod(methodIdentifier, usingMethodIdentifier);
                repository.registerMethodUseType(methodIdentifier, usingMethodIdentifier.declaringType());
            });
        });
    }
}
