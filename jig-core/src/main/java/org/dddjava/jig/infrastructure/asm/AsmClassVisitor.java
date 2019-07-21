package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.declaration.annotation.*;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldType;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodReturn;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.type.*;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.objectweb.asm.Type;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class AsmClassVisitor extends ClassVisitor {

    TypeByteCode typeByteCode;

    AsmClassVisitor() {
        super(Opcodes.ASM7);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);

        ParameterizedType parameterizedSuperType = parameterizedSuperType(superName, signature);

        ParameterizedTypes parameterizedInterfaceTypes = parameterizedInterfaceTypes(interfaces, signature);

        this.typeByteCode = new TypeByteCode(
                new TypeIdentifier(name),
                parameterizedSuperType,
                parameterizedInterfaceTypes,
                useTypes,
                (access & Opcodes.ACC_FINAL) == 0);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new MyAnnotationVisitor(this.api, typeDescriptorToIdentifier(descriptor), annotation ->
                typeByteCode.registerTypeAnnotation(new TypeAnnotation(annotation, typeByteCode.typeIdentifier()))
        );

    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        List<TypeIdentifier> genericsTypes = extractClassTypeFromGenericsSignature(signature);
        genericsTypes.forEach(typeByteCode::registerUseType);

        // 配列フィールドの型
        if (descriptor.charAt(0) == '[') {
            Type elementType = Type.getType(descriptor).getElementType();
            typeByteCode.registerUseType(toTypeIdentifier(elementType));
        }


        if ((access & Opcodes.ACC_STATIC) == 0) {
            FieldType fieldType = typeDescriptorToFieldType(descriptor, signature);
            FieldDeclaration fieldDeclaration = new FieldDeclaration(typeByteCode.typeIdentifier(), name, fieldType);
            // インスタンスフィールドだけ相手にする
            typeByteCode.registerField(fieldDeclaration);
            return new FieldVisitor(this.api) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    TypeIdentifier annotationTypeIdentifier = typeDescriptorToIdentifier(descriptor);
                    typeByteCode.registerUseType(annotationTypeIdentifier);
                    return new MyAnnotationVisitor(this.api, annotationTypeIdentifier, annotation ->
                            typeByteCode.registerFieldAnnotation(new FieldAnnotation(annotation, fieldDeclaration)));
                }
            };
        }
        if (!name.equals("$VALUES")) {
            StaticFieldDeclaration fieldDeclaration = new StaticFieldDeclaration(typeByteCode.typeIdentifier(), name, typeDescriptorToIdentifier(descriptor));
            // staticフィールドのうち、enumの $VALUES は除く
            typeByteCode.registerStaticField(fieldDeclaration);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodReturn methodReturn = extractParameterizedReturnType(signature, descriptor);

        MethodDeclaration methodDeclaration = new MethodDeclaration(typeByteCode.typeIdentifier(), toMethodSignature(name, descriptor), methodReturn);

        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);
        if (exceptions != null) {
            for (String exception : exceptions) {
                useTypes.add(new TypeIdentifier(exception));
            }
        }
        MethodByteCode methodByteCode = new MethodByteCode(methodDeclaration, useTypes, access);
        methodByteCode.bind(typeByteCode);

        return new MethodVisitor(this.api) {

            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.ACONST_NULL) {
                    methodByteCode.markReferenceNull();
                }
                super.visitInsn(opcode);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return new MyAnnotationVisitor(this.api, typeDescriptorToIdentifier(descriptor), annotation ->
                        methodByteCode.registerAnnotation(new MethodAnnotation(annotation, methodDeclaration)));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                FieldType fieldType = typeDescriptorToFieldType(descriptor);
                FieldDeclaration fieldDeclaration = new FieldDeclaration(new TypeIdentifier(owner), name, fieldType);
                methodByteCode.registerFieldInstruction(fieldDeclaration);

                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                methodByteCode.registerMethodInstruction(
                        new MethodDeclaration(new TypeIdentifier(owner), toMethodSignature(name, descriptor), new MethodReturn(methodDescriptorToReturnIdentifier(descriptor))));

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitLdcInsn(Object value) {
                if (value instanceof Type) {
                    // `Xxx.class` などのクラス参照を読み込む
                    methodByteCode.registerClassReference(toTypeIdentifier((Type) value));
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
                            methodByteCode.registerInvokeDynamic(toTypeIdentifier(type.getReturnType()));
                            for (Type argumentType : type.getArgumentTypes()) {
                                methodByteCode.registerInvokeDynamic(toTypeIdentifier(argumentType));
                            }
                        }
                    }

                    // lambdaで記述されているハンドラメソッド
                    if (bootstrapMethodArgument instanceof Handle) {
                        Handle handle = (Handle) bootstrapMethodArgument;
                        methodByteCode.registerMethodInstruction(
                                new MethodDeclaration(new TypeIdentifier(handle.getOwner()), toMethodSignature(handle.getName(), handle.getDesc()), new MethodReturn(methodDescriptorToReturnIdentifier(handle.getDesc())))
                        );
                    }
                }

                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }

            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                // switchがある
                methodByteCode.registerLookupSwitchInstruction();
                super.visitLookupSwitchInsn(dflt, keys, labels);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR) {
                    // 何かしらの分岐がある
                    methodByteCode.registerJumpInstruction();
                }

                if (opcode == Opcodes.IFNONNULL || opcode == Opcodes.IFNULL) {
                    methodByteCode.markJudgeNull();
                }
                super.visitJumpInsn(opcode, label);
            }
        };
    }

    private MethodSignature toMethodSignature(String name, String descriptor) {
        List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(this::toTypeIdentifier)
                .collect(Collectors.toList());
        return new MethodSignature(name, new Arguments(argumentTypes));
    }

    private TypeIdentifier methodDescriptorToReturnIdentifier(String descriptor) {
        return toTypeIdentifier(Type.getReturnType(descriptor));
    }

    private FieldType typeDescriptorToFieldType(String descriptor, String signature) {
        if (signature == null) {
            return typeDescriptorToFieldType(descriptor);
        }

        ArrayList<TypeIdentifier> typeParameters = new ArrayList<>();
        new SignatureReader(signature).accept(
                new SignatureVisitor(this.api) {
                    @Override
                    public SignatureVisitor visitTypeArgument(char wildcard) {
                        if (wildcard == '=') {
                            return new SignatureVisitor(this.api) {
                                @Override
                                public void visitClassType(String name) {
                                    typeParameters.add(new TypeIdentifier(name));
                                }
                            };
                        }
                        return super.visitTypeArgument(wildcard);
                    }
                }
        );
        TypeIdentifiers typeIdentifiers = new TypeIdentifiers(typeParameters);
        return new FieldType(typeDescriptorToIdentifier(descriptor), typeIdentifiers);
    }

    private FieldType typeDescriptorToFieldType(String descriptor) {
        TypeIdentifier typeIdentifier = typeDescriptorToIdentifier(descriptor);
        return new FieldType(typeIdentifier);
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

    private MethodReturn extractParameterizedReturnType(String signature, String descriptor) {

        TypeIdentifier[] collector = new TypeIdentifier[2];
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature != null) {
            new SignatureReader(signature).accept(
                    new SignatureVisitor(this.api) {

                        @Override
                        public SignatureVisitor visitReturnType() {
                            return new SignatureVisitor(this.api) {
                                @Override
                                public void visitClassType(String name) {
                                    // 戻り値の型
                                    collector[0] = new TypeIdentifier(name);
                                }

                                @Override
                                public SignatureVisitor visitTypeArgument(char wildcard) {
                                    if (wildcard != '=') {
                                        // 境界型は対応しない
                                        return super.visitTypeArgument(wildcard);
                                    }
                                    return new SignatureVisitor(this.api) {
                                        @Override
                                        public void visitClassType(String name) {
                                            // 型引数の型
                                            collector[1] = new TypeIdentifier(name);
                                        }
                                    };
                                }
                            };
                        }
                    }
            );
        }

        // ジェネリクスを使用しているが戻り値型が解決できないときの対応
        if (collector[0] == null) {
            TypeIdentifier returnTypeIdentifier = methodDescriptorToReturnIdentifier(descriptor);
            return new MethodReturn(returnTypeIdentifier);
        }

        return new MethodReturn(new ParameterizedType(collector[0], new TypeParameter(collector[1])));
    }

    private ParameterizedType parameterizedSuperType(String superName, String signature) {
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature == null) {
            return new ParameterizedType(new TypeIdentifier(superName));
        }

        SignatureVisitor noOpVisitor = new SignatureVisitor(this.api) {
        };

        List<TypeParameter> typeParameters = new ArrayList<>();

        new SignatureReader(signature).accept(
                new SignatureVisitor(this.api) {
                    @Override
                    public SignatureVisitor visitSuperclass() {

                        return new SignatureVisitor(this.api) {

                            @Override
                            public SignatureVisitor visitTypeArgument(char wildcard) {
                                if (wildcard == '=') {
                                    return new SignatureVisitor(this.api) {
                                        @Override
                                        public void visitClassType(String name) {
                                            typeParameters.add(new TypeParameter(new TypeIdentifier(name)));
                                        }

                                        @Override
                                        public SignatureVisitor visitTypeArgument(char wildcard) {
                                            // ジェネリクスのネストは対応しない
                                            return noOpVisitor;
                                        }
                                    };
                                }
                                // 境界型は対応しない
                                return noOpVisitor;
                            }
                        };
                    }
                }
        );

        return new ParameterizedType(new TypeIdentifier(superName), new TypeParameters(typeParameters));
    }

    private ParameterizedTypes parameterizedInterfaceTypes(String[] interfaces, String signature) {
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature == null) {
            // 非総称型で作成
            List<ParameterizedType> list = Arrays.stream(interfaces)
                    .map(TypeIdentifier::new)
                    .map(ParameterizedType::new)
                    .collect(Collectors.toList());
            return new ParameterizedTypes(list);
        }

        SignatureVisitor noOpVisitor = new SignatureVisitor(this.api) {
        };

        List<ParameterizedType> parameterizedTypes = new ArrayList<>();
        new SignatureReader(signature).accept(
                new SignatureVisitor(this.api) {
                    @Override
                    public SignatureVisitor visitInterface() {

                        return new SignatureVisitor(this.api) {
                            List<TypeParameter> typeParameters = new ArrayList<>();
                            String interfaceName;

                            @Override
                            public void visitClassType(String name) {
                                interfaceName = name;
                            }

                            @Override
                            public SignatureVisitor visitTypeArgument(char wildcard) {
                                if (wildcard == '=') {
                                    return new SignatureVisitor(this.api) {
                                        @Override
                                        public void visitClassType(String name) {
                                            typeParameters.add(new TypeParameter(new TypeIdentifier(name)));
                                        }

                                        @Override
                                        public SignatureVisitor visitTypeArgument(char wildcard) {
                                            // ジェネリクスのネストは対応しない
                                            return noOpVisitor;
                                        }
                                    };
                                }
                                // 境界型は対応しない
                                return noOpVisitor;
                            }

                            @Override
                            public void visitEnd() {
                                parameterizedTypes.add(new ParameterizedType(new TypeIdentifier(interfaceName), new TypeParameters(typeParameters)));
                            }
                        };
                    }
                }
        );

        return new ParameterizedTypes(parameterizedTypes);
    }

    private static class MyAnnotationVisitor extends AnnotationVisitor {
        final AnnotationDescription annotationDescription = new AnnotationDescription();
        private final TypeIdentifier annotationType;
        final Consumer<Annotation> finisher;

        public MyAnnotationVisitor(int api, TypeIdentifier annotationType, Consumer<Annotation> finisher) {
            super(api);
            this.annotationType = annotationType;
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

            return new AnnotationVisitor(api) {
                List<Object> list = new ArrayList<>();

                @Override
                public void visit(String name, Object value) {
                    list.add(value);
                }

                @Override
                public void visitEnd() {
                    annotationDescription.addArray(name, list);
                }
            };
        }

        @Override
        public void visitEnd() {
            finisher.accept(new Annotation(annotationType, annotationDescription));
        }
    }
}
