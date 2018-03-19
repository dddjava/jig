package jig.infrastructure.asm;

import jig.domain.model.specification.MethodDescriptor;
import org.objectweb.asm.*;

public class SpecificationReadingVisitor extends ClassVisitor {

    private final SpecificationBuilder specificationBuilder;

    public SpecificationReadingVisitor(SpecificationBuilder specificationBuilder) {
        super(Opcodes.ASM6);
        this.specificationBuilder = specificationBuilder;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.specificationBuilder
                .withName(name)
                .withInterfaces(interfaces)
                .withParent(superName)
                .withAccessor(access);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        this.specificationBuilder.withAnnotation(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // インスタンスフィールドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0) {
            this.specificationBuilder.withInstanceField(descriptor);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // インスタンスメソッドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {
            MethodDescriptor methodDescriptor = this.specificationBuilder.newInstanceMethod(name, descriptor);

            return new SpecificationReadingMethodVisitor(this.api, methodDescriptor);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
