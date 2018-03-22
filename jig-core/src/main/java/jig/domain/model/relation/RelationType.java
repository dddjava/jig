package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.specification.Specification;
import org.objectweb.asm.Type;

public enum RelationType {
    DEPENDENCY,
    FIELD,
    METHOD,
    METHOD_RETURN_TYPE,
    METHOD_PARAMETER,
    METHOD_USE_TYPE,
    IMPLEMENT,
    METHOD_USE_METHOD;

    public Relation of(Identifier from, Identifier to) {
        return new Relation(from, to, this);
    }

    public static void register(RelationRepository repository, Specification specification) {

        specification.fieldDescriptors.forEach(descriptor -> {
            Type fieldType = Type.getType(descriptor.toString());
            repository.registerField(specification.identifier, new Identifier(fieldType.getClassName()));
        });

        specification.methodSpecifications.forEach(methodSpecification -> {
            repository.registerMethod(specification.identifier, methodSpecification.identifier);

            Identifier returnTypeIdentifier = methodSpecification.getReturnTypeName();
            repository.registerMethodReturnType(methodSpecification.identifier, returnTypeIdentifier);

            for (Identifier argumentTypeIdentifier : methodSpecification.argumentTypeIdentifiers().list()) {
                repository.registerMethodParameter(methodSpecification.identifier, argumentTypeIdentifier);
            }

            for (Identifier interfaceIdentifier : specification.interfaceIdentifiers.list()) {
                repository.registerImplementation(methodSpecification.identifier, methodSpecification.methodIdentifierWith(interfaceIdentifier));
            }

            methodSpecification.usingFieldTypeIdentifiers.forEach(fieldTypeName -> {
                repository.registerMethodUseType(methodSpecification.identifier, fieldTypeName);
            });

            methodSpecification.usingMethodIdentifiers.forEach(methodName -> {
                repository.registerMethodUseMethod(methodSpecification.identifier, methodName);
            });
        });
    }
}
