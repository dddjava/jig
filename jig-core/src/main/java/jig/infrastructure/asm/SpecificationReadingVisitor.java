package jig.infrastructure.asm;

import jig.domain.model.specification.ClassDescriptor;
import jig.domain.model.specification.MethodSpecification;
import jig.domain.model.specification.Specification;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpecificationReadingVisitor extends ClassVisitor {

    private Name name;
    private Name parent;
    private List<String> annotationDescriptors = new ArrayList<>();
    private List<MethodSpecification> methodSpecifications = new ArrayList<>();
    private List<String> fieldDescriptors = new ArrayList<>();
    private Names interfaceNames;
    private int accessor;

    public SpecificationReadingVisitor() {
        super(Opcodes.ASM6);
    }

    public Specification specification() {
        return new Specification(
                name,
                parent,
                accessor,
                interfaceNames,
                annotationDescriptors.stream().map(ClassDescriptor::new).collect(Collectors.toList()),
                methodSpecifications,
                fieldDescriptors.stream().map(ClassDescriptor::new).collect(Collectors.toList()));
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = new Name(name);
        this.parent = new Name(superName);
        this.accessor = access;
        this.interfaceNames = Arrays.stream(interfaces).map(Name::new).collect(Names.collector());

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        this.annotationDescriptors.add(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // インスタンスフィールドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0) {
            fieldDescriptors.add(descriptor);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // インスタンスメソッドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {
            MethodSpecification methodSpecification = newInstanceMethod(name, descriptor);

            return new SpecificationReadingMethodVisitor(this.api, methodSpecification);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    public MethodSpecification newInstanceMethod(String methodName, String descriptor) {
        MethodSpecification methodSpecification = new MethodSpecification(name, methodName, descriptor);
        this.methodSpecifications.add(methodSpecification);
        return methodSpecification;
    }
}
