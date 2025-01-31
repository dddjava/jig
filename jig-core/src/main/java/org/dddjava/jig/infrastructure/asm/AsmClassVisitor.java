package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldType;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.type.*;
import org.dddjava.jig.domain.model.sources.JigMethodBuilder;
import org.dddjava.jig.domain.model.sources.JigTypeBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * visit
 * [ visitSource ]
 * [ visitModule ]
 * [ visitNestHost ][ visitOuterClass ]
 * ( visitAnnotation | visitTypeAnnotation | visitAttribute )*
 * (
 *     visitNestMember
 *   | [ * visitPermittedSubclass ]
 *   | visitInnerClass
 *   | visitRecordComponent
 *   | visitField
 *   | visitMethod
 * )*
 * visitEnd
 */
class AsmClassVisitor extends ClassVisitor {
    static Logger logger = LoggerFactory.getLogger(AsmClassVisitor.class);

    private JigTypeBuilder jigTypeBuilder;

    AsmClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    public JigTypeBuilder jigTypeBuilder() {
        // visitEnd後にしか呼んではいけない
        return Objects.requireNonNull(jigTypeBuilder);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // accessは https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.1-200-E.1
        List<ParameterizedType> actualTypeParameters = extractClassTypeFromGenericsSignature(signature).stream().map(ParameterizedType::new).collect(Collectors.toList());

        ParameterizedType type = new ParameterizedType(TypeIdentifier.valueOf(name), actualTypeParameters);

        ParameterizedType superType;
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature != null) {
            AsmClassSignatureVisitor asmClassSignatureVisitor = new AsmClassSignatureVisitor(api);
            new SignatureReader(signature).accept(asmClassSignatureVisitor);
            superType = asmClassSignatureVisitor.superclass();
        } else {
            superType = new ParameterizedType(TypeIdentifier.valueOf(superName));
        }

        List<ParameterizedType> interfaceTypes;
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature != null) {
            SignatureVisitor noOpVisitor = new SignatureVisitor(AsmClassVisitor.this.api) {
            };

            List<ParameterizedType> parameterizedTypes = new ArrayList<>();
            new SignatureReader(signature).accept(
                    new SignatureVisitor(AsmClassVisitor.this.api) {
                        @Override
                        public SignatureVisitor visitInterface() {

                            return new SignatureVisitor(this.api) {
                                final List<TypeIdentifier> typeParameters = new ArrayList<>();
                                String interfaceName;

                                @Override
                                public void visitClassType(String name1) {
                                    interfaceName = name1;
                                }

                                @Override
                                public SignatureVisitor visitTypeArgument(char wildcard) {
                                    if (wildcard == '=') {
                                        return new SignatureVisitor(this.api) {
                                            @Override
                                            public void visitClassType(String name1) {
                                                typeParameters.add(TypeIdentifier.valueOf(name1));
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
                                    parameterizedTypes.add(ParameterizedType.convert(TypeIdentifier.valueOf(interfaceName), typeParameters));
                                }
                            };
                        }
                    }
            );

            interfaceTypes = parameterizedTypes;
        } else {
            // 非総称型で作成
            interfaceTypes = Arrays.stream(interfaces)
                    .map(TypeIdentifier::valueOf)
                    .map(ParameterizedType::new)
                    .collect(Collectors.toList());
        }

        jigTypeBuilder = new JigTypeBuilder(type, superType, interfaceTypes, typeKind(access), resolveVisibility(access));

        super.visit(version, access, name, signature, superName, interfaces);
    }

    private TypeKind typeKind(int access) {
        if ((access & Opcodes.ACC_ENUM) != 0) {
            if ((access & Opcodes.ACC_FINAL) == 0) {
                return TypeKind.抽象列挙型;
            }
            return TypeKind.列挙型;
        }

        // FIXME: アノテーション、インタフェース、抽象型の判定が足りない

        // この判定できるのはASM固有
        if ((access & Opcodes.ACC_RECORD) != 0) {
            return TypeKind.レコード型;
        }

        return TypeKind.通常型;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AsmAnnotationVisitor(this.api, typeDescriptorToIdentifier(descriptor), annotation ->
                jigTypeBuilder.addAnnotation(annotation)
        );
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールド
            FieldType fieldType = typeDescriptorToFieldType(descriptor, signature);

            FieldDeclaration fieldDeclaration = jigTypeBuilder.addInstanceField(fieldType, name);
            return new FieldVisitor(this.api) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    TypeIdentifier annotationTypeIdentifier = typeDescriptorToIdentifier(descriptor);
                    return new AsmAnnotationVisitor(this.api, annotationTypeIdentifier,
                            annotation -> jigTypeBuilder.addFieldAnnotation(new FieldAnnotation(annotation, fieldDeclaration)));
                }
            };
        } else if (!name.equals("$VALUES")) {
            // staticフィールドのうち、enumにコンパイル時に作成される $VALUES は除く
            jigTypeBuilder.addStaticField(name, typeDescriptorToIdentifier(descriptor));
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    /**
     * {@link ClassReader} の読み取り順が recordComponent -> field -> method となっているので、
     * ここで recordComponent の名前を記録して field/method の判定に使える。
     */
    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        // name: 名前
        // descriptor: Type
        // ジェネリクスを使用している場合だけsignatureが入る

        jigTypeBuilder.addRecordComponent(name, typeDescriptorToIdentifier(descriptor));

        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // name: 名前
        // descriptor: (Type)Type 引数と戻り値の型ひとまとまり

        List<TypeIdentifier> signatureContainedTypes = extractClassTypeFromGenericsSignature(signature);

        return AsmMethodVisitor.from(this.api,
                access, name, descriptor, signature, exceptions,
                jigTypeBuilder.typeIdentifier(),
                data -> {
                    JigMethodBuilder jigMethodBuilder = createPlainMethodBuilder(
                            jigTypeBuilder,
                            access,
                            data.visibility,
                            signatureContainedTypes,
                            data.throwsTypes,
                            data.methodDeclaration);
                    jigMethodBuilder.setAnnotations(data.annotationList);
                    jigMethodBuilder.setInstructions(data.methodInstructions);
                });
    }

    /**
     * Visibilityに持っていきたいが、accessの定数はasmが持っているのでここに置いておく。
     * 実際はバイトコードの固定値。
     *
     * classの場合、ソースコードではpublic,protected,default,privateは定義できるが、
     * バイトコードではpublicか否かしか識別できない。
     * さらにprotectedもpublicになる。（パッケージ外から参照可能なので。）
     */
    private TypeVisibility resolveVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return TypeVisibility.PUBLIC;
        return TypeVisibility.NOT_PUBLIC;
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
                                    typeParameters.add(TypeIdentifier.valueOf(name));
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

    static TypeIdentifier typeDescriptorToIdentifier(String descriptor) {
        Type type = Type.getType(descriptor);
        return TypeIdentifier.valueOf(type.getClassName());
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
                            useTypes.add(TypeIdentifier.valueOf(name));
                        }
                    }
            );
        }
        return useTypes;
    }

    public JigMethodBuilder createPlainMethodBuilder(JigTypeBuilder jigTypeBuilder,
                                                     int access,
                                                     Visibility visibility,
                                                     List<TypeIdentifier> signatureContainedTypes,
                                                     List<TypeIdentifier> throwsTypes,
                                                     MethodDeclaration methodDeclaration) {
        MethodDerivation methodDerivation = resolveMethodDerivation(methodDeclaration.methodSignature(), methodDeclaration.methodReturn(), access);
        var jigMethodBuilder = new JigMethodBuilder(methodDeclaration, signatureContainedTypes, visibility, methodDerivation, throwsTypes);

        if (methodDeclaration.isConstructor()) {
            // コンストラクタ
            jigTypeBuilder.constructorFacts(jigMethodBuilder);
        } else if ((access & Opcodes.ACC_STATIC) != 0) {
            // staticメソッド
            jigTypeBuilder.staticJigMethodBuilders(jigMethodBuilder);
        } else {
            // コンストラクタでもstaticメソッドでもない＝インスタンスメソッド
            jigTypeBuilder.instanceJigMethodBuilders(jigMethodBuilder);
        }

        return jigMethodBuilder;
    }

    private MethodDerivation resolveMethodDerivation(MethodSignature methodSignature, MethodReturn methodReturn, int access) {
        String name = methodSignature.methodName();
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return MethodDerivation.CONSTRUCTOR;
        }

        if ((access & Opcodes.ACC_BRIDGE) != 0 || (access & Opcodes.ACC_SYNTHETIC) != 0) {
            return MethodDerivation.COMPILER_GENERATED;
        }

        if (jigTypeBuilder.isRecordComponent(methodSignature, methodReturn)) {
            return MethodDerivation.RECORD_COMPONENT;
        }

        if (jigTypeBuilder.superType().typeIdentifier().isEnum() && (access & Opcodes.ACC_STATIC) != 0) {
            // enumで生成されるstaticメソッド2つをコンパイラ生成として扱う
            if (methodSignature.isSame(new MethodSignature("values"))) {
                return MethodDerivation.COMPILER_GENERATED;
            } else {
                if (methodSignature.isSame(new MethodSignature("valueOf", TypeIdentifier.from(String.class)))) {
                    return MethodDerivation.COMPILER_GENERATED;
                }
            }
        }

        return MethodDerivation.PROGRAMMER;
    }

}
