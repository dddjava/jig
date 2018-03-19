package jig.infrastructure.asm;

import org.objectweb.asm.*;

import java.util.logging.Logger;

public class SpecificationReadingVisiter extends ClassVisitor {

    private static final Logger LOGGER = Logger.getLogger(SpecificationReadingVisiter.class.getName());

    private final SpecificationBuilder specificationBuilder;

    public SpecificationReadingVisiter(SpecificationBuilder specificationBuilder) {
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
            this.specificationBuilder.withInstanceMethod(name, descriptor);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {

        super.visitEnd();
    }
}
