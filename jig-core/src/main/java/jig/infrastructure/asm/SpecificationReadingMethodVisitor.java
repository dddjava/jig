package jig.infrastructure.asm;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.specification.MethodSpecification;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.stream.Collectors;

class SpecificationReadingMethodVisitor extends MethodVisitor {

    private final MethodSpecification methodSpecification;

    public SpecificationReadingMethodVisitor(int api, MethodSpecification methodSpecification) {
        super(api);
        this.methodSpecification = methodSpecification;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        methodSpecification.registerAnnotation(new TypeIdentifier(Type.getType(descriptor).getClassName()));

        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        TypeIdentifier ownerType = new TypeIdentifier(owner);
        TypeIdentifier fieldType = new TypeIdentifier(Type.getType(descriptor).getClassName());
        FieldIdentifier field = new FieldIdentifier(name, fieldType);
        methodSpecification.registerUsingField(ownerType, field);

        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        TypeIdentifier returnType = new TypeIdentifier(Type.getReturnType(descriptor).getClassName());
        TypeIdentifier ownerType = new TypeIdentifier(owner);
        MethodSignature methodSignature = new MethodSignature(name,
                Arrays.stream(Type.getArgumentTypes(descriptor))
                        .map(Type::getClassName)
                        .map(TypeIdentifier::new)
                        .collect(Collectors.toList()));
        MethodIdentifier methodIdentifier = new MethodIdentifier(ownerType, methodSignature);

        methodSpecification.registerMethodInstruction(ownerType, methodIdentifier, returnType);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof Type) {
            String className = Type.class.cast(value).getClassName();
            TypeIdentifier type = new TypeIdentifier(className);
            methodSpecification.registerClassReference(type);
        }
        super.visitLdcInsn(value);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        for (Object bootstrapMethodArgument : bootstrapMethodArguments) {
            if (bootstrapMethodArgument instanceof Type) {
                Type type = (Type) bootstrapMethodArgument;
                if (type.getSort() == Type.METHOD) {
                    Type returnType = type.getReturnType();
                    methodSpecification.registerInvokeDynamic(new TypeIdentifier(returnType.getClassName()));
                    for (Type argumentType : type.getArgumentTypes()) {
                        methodSpecification.registerInvokeDynamic(new TypeIdentifier(argumentType.getClassName()));
                    }
                }
            }
        }
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }
}
