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
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

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
        List<TypeIdentifier> useTypes = extractSignatureClassType(signature);

        this.specification = new Specification(
                new TypeIdentifier(name),
                new TypeIdentifier(superName),
                access,
                Arrays.stream(interfaces).map(TypeIdentifier::new).collect(TypeIdentifiers.collector()),
                useTypes
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
        if (signature != null) {
            new SignatureReader(signature).acceptType(
                    new SignatureVisitor(this.api) {
                        @Override
                        public void visitClassType(String name) {
                            specification.addUseType(new TypeIdentifier(name));
                        }
                    }
            );
        }

        // 配列フィールドの型
        if (descriptor.charAt(0) == '[') {
            Type elementType = Type.getType(descriptor).getElementType();
            specification.addUseType(new TypeIdentifier(elementType.getClassName()));
        }

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

        List<TypeIdentifier> useTypes = extractSignatureClassType(signature);
        if (exceptions != null) {
            for (String exception : exceptions) {
                useTypes.add(new TypeIdentifier(exception));
            }
        }

        MethodSpecification methodSpecification = new MethodSpecification(
                identifier,
                new TypeIdentifier(Type.getReturnType(descriptor).getClassName()),
                useTypes,
                (access & Opcodes.ACC_STATIC) == 0 && !identifier.methodSignature().asSimpleText().startsWith("<init>")
        );
        specification.add(methodSpecification);
        return new SpecificationReadingMethodVisitor(this.api, methodSpecification);
    }

    private List<TypeIdentifier> extractSignatureClassType(String signature) {
        List<TypeIdentifier> useTypes = new ArrayList<>();
        if (signature != null) {
            // ジェネリクスを使用している場合だけsignatureが入る
            new SignatureReader(signature).accept(
                    new SignatureVisitor(this.api) {
                        @Override
                        public void visitClassType(String name) {
                            // 引数と戻り値に登場するクラスを収集
                            useTypes.add(new TypeIdentifier(name));
                        }
                    }
            );
        }
        return useTypes;
    }
}
