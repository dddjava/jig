package jig.infrastructure.asm;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.specification.MethodSpecification;
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
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        TypeIdentifier fieldType = new TypeIdentifier(Type.getType(descriptor).getClassName());
        FieldIdentifier field = new FieldIdentifier(name, fieldType);
        methodSpecification.registerUsingField(field);

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
}
