package jig.infrastructure.asm;

import jig.domain.model.specification.MethodSpecification;
import org.objectweb.asm.MethodVisitor;

class SpecificationReadingMethodVisitor extends MethodVisitor {

    private final MethodSpecification methodSpecification;

    public SpecificationReadingMethodVisitor(int api, MethodSpecification methodSpecification) {
        super(api);
        this.methodSpecification = methodSpecification;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        methodSpecification.addFieldInstruction(owner, name, descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        methodSpecification.addMethodInstruction(owner, name, descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
