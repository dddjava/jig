package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.specification.MethodSpecification;
import org.dddjava.jig.domain.model.specification.Specification;
import org.dddjava.jig.domain.model.specification.SpecificationContext;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class SpecificationReadingVisitor extends ClassVisitor {

    final SpecificationContext specificationContext;
    Specification specification;

    public SpecificationReadingVisitor(SpecificationContext specificationContext) {
        super(Opcodes.ASM6);
        this.specificationContext = specificationContext;
    }

    public Specification specification() {
        return specification;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);

        this.specification = new Specification(
                specificationContext,
                new TypeIdentifier(name),
                new TypeIdentifier(superName),
                Arrays.stream(interfaces).map(TypeIdentifier::new).collect(TypeIdentifiers.collector()),
                useTypes,
                (access & Opcodes.ACC_FINAL) == 0);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        TypeAnnotationDeclaration typeAnnotationDeclaration = specification.newAnnotationDeclaration(typeDescriptorToIdentifier(descriptor));
        specification.registerTypeAnnotation(typeAnnotationDeclaration);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        List<TypeIdentifier> genericsTypes = extractClassTypeFromGenericsSignature(signature);
        genericsTypes.forEach(specification::registerUseType);

        // 配列フィールドの型
        if (descriptor.charAt(0) == '[') {
            Type elementType = Type.getType(descriptor).getElementType();
            specification.registerUseType(toTypeIdentifier(elementType));
        }

        FieldDeclaration fieldDeclaration = specification.newFieldDeclaration(name, typeDescriptorToIdentifier(descriptor));

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールドだけ相手にする
            specification.registerField(fieldDeclaration);
        } else {
            if (!name.equals("$VALUES")) {
                // 定数だけどenumの $VALUES は除く
                specification.registerStaticField(fieldDeclaration);
            }
        }
        return new FieldVisitor(this.api) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                TypeIdentifier annotationTypeIdentifier = typeDescriptorToIdentifier(descriptor);
                specification.registerUseType(annotationTypeIdentifier);
                return new MyAnnotationVisitor(this.api, annotationDescription ->
                        specification.registerFieldAnnotation(new FieldAnnotationDeclaration(fieldDeclaration, annotationTypeIdentifier, annotationDescription)));
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodDeclaration methodDeclaration = new MethodDeclaration(specification.typeIdentifier(), toMethodSignature(name, descriptor), methodDescriptorToReturnIdentifier(descriptor));

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
                access
        );
        if (methodSpecification.isConstructor()) {
            // コンストラクタ
            specification.registerConstructorSpecification(methodSpecification);
        } else if (methodSpecification.isStatic()) {
            // staticメソッド
            specification.registerStaticMethodSpecification(methodSpecification);
        } else {
            // インスタンスメソッド
            specification.registerInstanceMethodSpecification(methodSpecification);
        }

        return new MethodVisitor(this.api) {

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return new MyAnnotationVisitor(this.api, annotationDescription ->
                        methodSpecification.registerAnnotation(new MethodAnnotationDeclaration(methodDeclaration, typeDescriptorToIdentifier(descriptor), annotationDescription)));
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
                        new MethodDeclaration(
                                new TypeIdentifier(owner),
                                toMethodSignature(name, descriptor),
                                methodDescriptorToReturnIdentifier(descriptor)));

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitLdcInsn(Object value) {
                if (value instanceof Type) {
                    // `Xxx.class` などのクラス参照を読み込む
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
                            // lambdaやメソッドリファレンスの引数と戻り値型を読み込む
                            methodSpecification.registerInvokeDynamic(toTypeIdentifier(type.getReturnType()));
                            for (Type argumentType : type.getArgumentTypes()) {
                                methodSpecification.registerInvokeDynamic(toTypeIdentifier(argumentType));
                            }
                        }
                    }
                }

                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }

            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                // switchがある
                methodSpecification.registerLookupSwitchInstruction();
                super.visitLookupSwitchInsn(dflt, keys, labels);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                // 何かしらの分岐がある
                methodSpecification.registerJumpInstruction();
                super.visitJumpInsn(opcode, label);
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

    private static class MyAnnotationVisitor extends AnnotationVisitor {
        final AnnotationDescription annotationDescription = new AnnotationDescription();
        final Consumer<AnnotationDescription> finisher;

        public MyAnnotationVisitor(int api, Consumer<AnnotationDescription> finisher) {
            super(api);
            this.finisher = finisher;
        }

        @Override
        public void visit(String name, Object value) {
            annotationDescription.addParam(name, value);
            super.visit(name, value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            annotationDescription.addEnum(name, value);
            super.visitEnum(name, descriptor, value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            annotationDescription.addAnnotation(name, descriptor);
            return super.visitAnnotation(name, descriptor);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            annotationDescription.addArray(name);
            return super.visitArray(name);
        }

        @Override
        public void visitEnd() {
            finisher.accept(annotationDescription);
        }
    }
}
