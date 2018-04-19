package jig.infrastructure.asm;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.specification.ClassDescriptor;
import jig.domain.model.specification.MethodSpecification;
import jig.domain.model.specification.Specification;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class SpecificationReadingVisitor extends ClassVisitor {

    private Specification specification;

    public SpecificationReadingVisitor() {
        super(Opcodes.ASM6);
    }

    public Specification specification() {
        return specification;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.specification = new Specification(
                new TypeIdentifier(name),
                new TypeIdentifier(superName),
                access,
                Arrays.stream(interfaces).map(TypeIdentifier::new).collect(TypeIdentifiers.collector())
        );
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        specification.addAnnotation(new ClassDescriptor(descriptor));
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        TypeIdentifier typeIdentifier = new ClassDescriptor(descriptor).toTypeIdentifier();
        FieldIdentifier field = new FieldIdentifier(name, typeIdentifier);

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールドだけ相手にする
            specification.add(field);
        } else {
            if (!name.equals("$VALUES")) {
                // 定数だけどenumの $VALUES は除く
                specification.addConstant(field);
            }
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(Type::getClassName)
                .map(TypeIdentifier::new)
                .collect(Collectors.toList());

        MethodIdentifier identifier = new MethodIdentifier(specification.typeIdentifier, new MethodSignature(name, argumentTypes));

        List<TypeIdentifier> exceptionTypes = new ArrayList<>();
        if (exceptions != null) {
            for (String exception : exceptions) {
                exceptionTypes.add(new TypeIdentifier(exception));
            }
        }

        MethodSpecification methodSpecification = new MethodSpecification(
                identifier,
                new TypeIdentifier(Type.getReturnType(descriptor).getClassName()),
                argumentTypes,
                exceptionTypes,
                (access & Opcodes.ACC_STATIC) == 0 && !identifier.methodSignature().asSimpleText().startsWith("<init>")
        );
        specification.add(methodSpecification);
        return new SpecificationReadingMethodVisitor(this.api, methodSpecification);
    }
}
