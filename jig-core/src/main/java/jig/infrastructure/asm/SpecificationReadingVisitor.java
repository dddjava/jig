package jig.infrastructure.asm;

import jig.domain.model.declaration.annotation.AnnotationDeclaration;
import jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
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
        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);

        this.specification = new Specification(
                new TypeIdentifier(name),
                new TypeIdentifier(superName),
                Arrays.stream(interfaces).map(TypeIdentifier::new).collect(TypeIdentifiers.collector()),
                useTypes,
                (access & Opcodes.ACC_FINAL) == 0);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationDeclaration annotationDeclaration = new AnnotationDeclaration(
                specification.typeIdentifier,
                typeDescriptorToIdentifier(descriptor)
        );
        specification.addAnnotation(annotationDeclaration);
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
            specification.addUseType(toTypeIdentifier(elementType));
        }

        TypeIdentifier typeIdentifier = typeDescriptorToIdentifier(descriptor);
        FieldDeclaration fieldDeclaration = new FieldDeclaration(specification.typeIdentifier, name, typeIdentifier);

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールドだけ相手にする
            specification.add(fieldDeclaration);
        } else {
            if (!name.equals("$VALUES")) {
                // 定数だけどenumの $VALUES は除く
                specification.addConstant(fieldDeclaration);
            }
        }
        return new FieldVisitor(this.api) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                TypeIdentifier annotationTypeIdentifier = typeDescriptorToIdentifier(descriptor);

                specification.addFieldAnnotation(new FieldAnnotationDeclaration(
                        fieldDeclaration,
                        annotationTypeIdentifier
                ));
                specification.addUseType(annotationTypeIdentifier);

                return super.visitAnnotation(descriptor, visible);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodDeclaration methodDeclaration = new MethodDeclaration(specification.typeIdentifier, toMethodSignature(name, descriptor));

        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);
        if (exceptions != null) {
            for (String exception : exceptions) {
                useTypes.add(new TypeIdentifier(exception));
            }
        }

        MethodSpecification methodSpecification = new MethodSpecification(
                methodDeclaration,
                methodDescriptorToReturnIdentifier(descriptor),
                useTypes,
                (access & Opcodes.ACC_STATIC) == 0 && !methodDeclaration.methodSignature().asSimpleText().startsWith("<init>")
        );
        specification.add(methodSpecification);

        return new MethodVisitor(this.api) {

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                methodSpecification.registerAnnotation(
                        new MethodAnnotationDeclaration(methodSpecification.methodDeclaration, typeDescriptorToIdentifier(descriptor)));

                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                methodSpecification.registerFieldInstruction(
                        new FieldDeclaration(new TypeIdentifier(owner), name, typeDescriptorToIdentifier(descriptor)));

                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                methodSpecification.registerMethodInstruction(
                        new MethodDeclaration(new TypeIdentifier(owner), toMethodSignature(name, descriptor)),
                        methodDescriptorToReturnIdentifier(descriptor));

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

        };
    }

    private MethodSignature toMethodSignature(String name, String descriptor) {
        List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(this::toTypeIdentifier)
                .collect(Collectors.toList());
        return new MethodSignature(name, argumentTypes);
    }

    private TypeIdentifier methodDescriptorToReturnIdentifier(String descriptor) {
        return toTypeIdentifier(Type.getReturnType(descriptor));
    }

    private TypeIdentifier typeDescriptorToIdentifier(String descriptor) {
        Type type = Type.getType(descriptor);
        return toTypeIdentifier(type);
    }

    private TypeIdentifier toTypeIdentifier(Type type) {
        return new TypeIdentifier(type.getClassName());
    }

    private List<TypeIdentifier> extractClassTypeFromGenericsSignature(String signature) {
        // ジェネリクスを使用している場合だけsignatureが入る
        List<TypeIdentifier> useTypes = new ArrayList<>();
        if (signature != null) {
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
