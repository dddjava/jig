package jig.domain.model.relation;

import jig.domain.model.specification.MethodDescriptor;
import jig.domain.model.specification.Specification;
import jig.domain.model.thing.Name;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum RelationType {
    DEPENDENCY,
    FIELD,
    METHOD,
    METHOD_RETURN_TYPE,
    METHOD_PARAMETER,
    METHOD_USE_TYPE,
    IMPLEMENT,
    METHOD_USE_METHOD;

    public Relation of(Name from, Name to) {
        return new Relation(from, to, this);
    }

    public static void register(RelationRepository repository, Specification specification) {

        specification.interfaceNames.list().forEach(name -> {
            repository.register(new Relation(specification.name, name, IMPLEMENT));
        });

        specification.fieldDescriptors.forEach(descriptor -> {
            Type fieldType = Type.getType(descriptor.toString());
            repository.register(RelationType.FIELD.of(specification.name, new Name(fieldType.getClassName())));
        });

        specification.methodDescriptors.forEach(methodDescriptor -> {
            String descriptor = methodDescriptor.descriptor;
            String name = methodDescriptor.methodName;
            Name className = specification.name;

            // パラメーターの型
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);

            // メソッド
            String argumentsString = Arrays.stream(argumentTypes).map(Type::getClassName).collect(Collectors.joining(",", "(", ")"));
            Name methodName = new Name(className.value() + "." + name + argumentsString);
            repository.register(RelationType.METHOD.of(className, methodName));

            // 戻り値の型
            Name returnTypeName = methodDescriptor.getReturnTypeName();
            repository.register(RelationType.METHOD_RETURN_TYPE.of(methodName, returnTypeName));

            for (Type type : argumentTypes) {
                Name argumentTypeName = new Name(type.getClassName());
                repository.register(RelationType.METHOD_PARAMETER.of(methodName, argumentTypeName));
            }

            for (Name interfaceName : specification.interfaceNames.list()) {
                repository.register(RelationType.IMPLEMENT.of(methodName, interfaceName.concat(methodName)));
            }

            registerMethodInstruction(repository, methodDescriptor);
        });
    }

    private static void registerMethodInstruction(RelationRepository repository, MethodDescriptor methodDescriptor) {
        methodDescriptor.usingFieldTypeNames.forEach(fieldTypeName -> {
            repository.register(RelationType.METHOD_USE_TYPE.of(methodDescriptor.name, fieldTypeName));
        });

        methodDescriptor.usingMethodNames.forEach(methodName -> {
            repository.register(RelationType.METHOD_USE_METHOD.of(methodDescriptor.name, methodName));
        });
    }
}
