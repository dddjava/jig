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
        methodSpecification.registerAnnotation(typeDescriptorToIdentifier(descriptor));

        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        methodSpecification.registerFieldInstruction(
                new FieldIdentifier(new TypeIdentifier(owner), name, typeDescriptorToIdentifier(descriptor)));

        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MethodSignature methodSignature = new MethodSignature(name,
                Arrays.stream(Type.getArgumentTypes(descriptor))
                        .map(this::toTypeIdentifier)
                        .collect(Collectors.toList()));
        MethodIdentifier methodIdentifier = new MethodIdentifier(new TypeIdentifier(owner), methodSignature);

        methodSpecification.registerMethodInstruction(methodIdentifier, methodDescriptorToReturnIdentifier(descriptor));

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof Type) {
            methodSpecification.registerClassReference(toTypeIdentifier((Type) value));
        }

        super.visitLdcInsn(value);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        for (Object bootstrapMethodArgument : bootstrapMethodArguments) {
            if (bootstrapMethodArgument instanceof Type) {
                Type type = (Type) bootstrapMethodArgument;
                if (type.getSort() == Type.METHOD) {
                    methodSpecification.registerInvokeDynamic(toTypeIdentifier(type.getReturnType()));
                    for (Type argumentType : type.getArgumentTypes()) {
                        methodSpecification.registerInvokeDynamic(toTypeIdentifier(argumentType));
                    }
                }
            }
        }

        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    private TypeIdentifier typeDescriptorToIdentifier(String descriptor) {
        return toTypeIdentifier(Type.getType(descriptor));
    }

    private TypeIdentifier methodDescriptorToReturnIdentifier(String descriptor) {
        return toTypeIdentifier(Type.getReturnType(descriptor));
    }

    private TypeIdentifier toTypeIdentifier(Type type) {
        return new TypeIdentifier(type.getClassName());
    }
}
