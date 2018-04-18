package jig.infrastructure.asm;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.specification.ClassDescriptor;
import jig.domain.model.specification.MethodSpecification;
import jig.domain.model.specification.Specification;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SpecificationReadingVisitor extends ClassVisitor {

    private TypeIdentifier typeIdentifier;
    private List<ClassDescriptor> annotationDescriptors = new ArrayList<>();
    private List<MethodSpecification> methodSpecifications = new ArrayList<>();
    private List<FieldIdentifier> fieldDescriptors = new ArrayList<>();
    private List<FieldIdentifier> constantDescriptors = new ArrayList<>();

    private Specification specification;

    public SpecificationReadingVisitor() {
        super(Opcodes.ASM6);
    }

    public Specification specification() {
        return specification;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.typeIdentifier = new TypeIdentifier(name);
        TypeIdentifier superType = new TypeIdentifier(superName);
        this.specification = new Specification(
                typeIdentifier,
                superType,
                access,
                Arrays.stream(interfaces).map(TypeIdentifier::new).collect(TypeIdentifiers.collector()),
                annotationDescriptors,
                methodSpecifications,
                fieldDescriptors,
                constantDescriptors);
        this.specification.addUseType(superType);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        this.annotationDescriptors.add(new ClassDescriptor(descriptor));
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        TypeIdentifier typeIdentifier = new ClassDescriptor(descriptor).toTypeIdentifier();
        FieldIdentifier field = new FieldIdentifier(name, typeIdentifier);
        specification.addUseType(typeIdentifier);

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールドだけ相手にする
            fieldDescriptors.add(field);
        } else {
            if (!name.equals("$VALUES")) {
                // 定数だけどenumの $VALUES は除く
                constantDescriptors.add(field);
            }
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // インスタンスメソッドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {
            MethodSpecification methodSpecification = newInstanceMethod(name, descriptor, exceptions);

            return new SpecificationReadingMethodVisitor(this.api, methodSpecification);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    public MethodSpecification newInstanceMethod(String methodName, String descriptor, String[] exceptions) {
        MethodSpecification methodSpecification = new MethodSpecification(typeIdentifier, methodName, descriptor, exceptions);
        this.methodSpecifications.add(methodSpecification);
        return methodSpecification;
    }
}
