package jig.infrastructure.asm;

import jig.domain.model.specification.MethodDescriptor;
import org.objectweb.asm.MethodVisitor;

class SpecificationReadingMethodVisitor extends MethodVisitor {

    private final MethodDescriptor methodDescriptor;

    public SpecificationReadingMethodVisitor(int api, MethodDescriptor methodDescriptor) {
        super(api);
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        methodDescriptor.addFieldInstruction(owner, name, descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        methodDescriptor.addMethodInstruction(owner, name, descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
