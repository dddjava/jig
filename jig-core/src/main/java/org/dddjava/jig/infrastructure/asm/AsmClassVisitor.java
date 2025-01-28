package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldType;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokeDynamicInstruction;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.data.classes.method.instruction.MethodInstructionType;
import org.dddjava.jig.domain.model.data.classes.type.*;
import org.dddjava.jig.domain.model.sources.JigMethodBuilder;
import org.dddjava.jig.domain.model.sources.JigTypeBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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
        ParameterizedType superType = superType(superName, signature);
        List<ParameterizedType> interfaceTypes = interfaceTypes(interfaces, signature);
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

        MethodDeclaration methodDeclaration = Optional.ofNullable(signature)
                .flatMap(nonNullSignature ->
                        // signatureがあればこちらから構築する
                        AsmMethodSignatureVisitor.buildMethodDeclaration(this.api, name, this.jigTypeBuilder.typeIdentifier(), nonNullSignature)
                ).orElseGet(() -> {
                    // signatureがないもしくは失敗した場合はdescriptorから構築する
                    // signatureの解析失敗はともかく、descriptorしかない場合はこの生成で適切なMethodSignatureができる

                    // descriptorから戻り値型を生成
                    MethodReturn methodReturn = MethodReturn.fromTypeOnly(methodDescriptorToReturnIdentifier(descriptor));
                    // descriptorから引数型を生成
                    List<ParameterizedType> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                            .map(AsmClassVisitor::toTypeIdentifier)
                            .map(ParameterizedType::noneGenerics)
                            .collect(Collectors.toList());
                    var methodSignature = MethodSignature.from(name, argumentTypes);
                    return new MethodDeclaration(jigTypeBuilder.typeIdentifier(), methodSignature, methodReturn);
                });

        List<TypeIdentifier> signatureContainedTypes = extractClassTypeFromGenericsSignature(signature);

        List<TypeIdentifier> throwsTypes = new ArrayList<>();
        if (exceptions != null) {
            for (String exception : exceptions) {
                throwsTypes.add(TypeIdentifier.valueOf(exception));
            }
        }

        var methodInstructions = Instructions.newInstance();
        JigMethodBuilder jigMethodBuilder = createPlainMethodBuilder(
                jigTypeBuilder,
                access,
                resolveMethodVisibility(access),
                signatureContainedTypes,
                throwsTypes,
                methodInstructions,
                methodDeclaration);

        return new MethodVisitor(this.api) {

            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.ACONST_NULL) {
                    methodInstructions.register(MethodInstructionType.NULL参照);
                }
                super.visitInsn(opcode);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return new AsmAnnotationVisitor(this.api, typeDescriptorToIdentifier(descriptor),
                        annotation -> jigMethodBuilder.addAnnotation(annotation));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                TypeIdentifier declaringType = TypeIdentifier.valueOf(owner);
                TypeIdentifier fieldTypeIdentifier = typeDescriptorToIdentifier(descriptor);

                methodInstructions.registerField(declaringType, fieldTypeIdentifier, name);
                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                        .map(AsmClassVisitor::toTypeIdentifier)
                        .toList();
                TypeIdentifier returnType = methodDescriptorToReturnIdentifier(descriptor);

                InvokedMethod invokedMethod = new InvokedMethod(TypeIdentifier.valueOf(owner), name, argumentTypes, returnType);
                methodInstructions.registerMethod(invokedMethod);
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitLdcInsn(Object value) {
                if (value instanceof Type typeValue) {
                    // `Xxx.class` などのクラス参照を読み込む
                    var typeIdentifier = toTypeIdentifier(typeValue);
                    methodInstructions.registerClassReference(typeIdentifier);
                }

                super.visitLdcInsn(value);
            }

            /**
             * invokeDynamicを処理する。
             * 通常はLambdaやメソッド参照を記述した場合だが、JVM言語を使用すると不意に現れる可能性がある。
             * ここではJava言語でのLambdaやメソッド参照と想定して処理している。
             */
            @Override
            public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                // name, descriptorにはLambdaやメソッド参照を受ける型の情報。
                // たとえばFunctionで受けるなら name=apply descriptor=()Ljava/util/function/Function; となる。
                // invokeDynamic実行時点でのこの情報あまり意味がないので使用しない。（必要であれば他のメソッド呼び出し時の引数として登場するはず。）

                // bootstrapMethodHandleはinvokedynamicの起動メソッドが入る。
                // Lambdaの場合は LambdaMetafactory#metafactory になり、他にも以下のようなものがある。
                // - 文字列の+での連結: StringConcatFactory#makeConcatWithConstants
                // - recordのtoStringなど: ObjectMethods#bootstrap
                // これ自体はアプリケーションコード実装者が意識するものでないので使用しない。

                // JavaでのLambdaやメソッド参照のみを処理する
                if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner())
                        && "metafactory".equals(bootstrapMethodHandle.getName())) {
                    if (bootstrapMethodArguments.length != 3) {
                        logger.warn("想定外のInvokeDynamicが {} で検出されました。読み飛ばします。", jigMethodBuilder.methodIdentifier());
                    } else {
                        // 0: Type 実装時の型。ジェネリクスなどは無視されるため、Functionの場合は (LObject;)LObject となる。
                        // 1: Handle: メソッド参照の場合は対象のメソッドのシグネチャ、Lambda式の場合は生成されたLambdaのシグネチャ
                        // 2: Type 動的に適用される型。ジェネリクスなども解決される。Lambdaを受けるインタフェースがジェネリクスを使用していない場合は 0 と同じになる。
                        // 0は無視して1,2を参照する。
                        if (bootstrapMethodArguments[1] instanceof Handle handle && isMethodRef(handle)
                                && bootstrapMethodArguments[2] instanceof Type type && type.getSort() == Type.METHOD) {
                            var handleOwnerType = TypeIdentifier.valueOf(handle.getOwner());
                            var handleMethodName = handle.getName();
                            var handleArgumentTypes = Arrays.stream(Type.getArgumentTypes(handle.getDesc()))
                                    .map(AsmClassVisitor::toTypeIdentifier)
                                    .collect(Collectors.toList());
                            var handleReturnType = methodDescriptorToReturnIdentifier(handle.getDesc());
                            var handleInvokeMethod = new InvokedMethod(handleOwnerType, handleMethodName, handleArgumentTypes, handleReturnType);

                            var returnType = toTypeIdentifier(type.getReturnType());
                            var argumentTypes = Arrays.stream(type.getArgumentTypes()).map(t -> toTypeIdentifier(t)).toList();

                            methodInstructions.registerInvokeDynamic(new InvokeDynamicInstruction(handleInvokeMethod, returnType, argumentTypes));
                        }
                    }
                }

                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }

            private boolean isMethodRef(Handle handle) {
                return switch (handle.getTag()) {
                    // フィールドに対する操作なので無視
                    case Opcodes.H_GETFIELD,
                         Opcodes.H_GETSTATIC,
                         Opcodes.H_PUTFIELD,
                         Opcodes.H_PUTSTATIC -> false;
                    // メソッドに関連するもの
                    case Opcodes.H_INVOKEVIRTUAL,
                         Opcodes.H_INVOKESTATIC,
                         Opcodes.H_INVOKESPECIAL,
                         Opcodes.H_NEWINVOKESPECIAL,
                         Opcodes.H_INVOKEINTERFACE -> true;
                    default -> {
                        // JVMとASMの仕様上ここには来ないはずだが、来た場合に続行不能にしたいためにログ出力しておく。
                        // 将来のJavaバージョンアップで追加された場合に
                        logger.warn("予期しないHandler {} が検出されました。解析が部分的にスキップされます。このログが出力される場合、lambdaによるメソッド呼び出しが欠落する可能性があります。issueなどで再現コードをいただけると助かります。", handle);
                        yield false;
                    }
                };
            }

            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                // switchがある
                methodInstructions.register(MethodInstructionType.SWITCH);
                super.visitLookupSwitchInsn(dflt, keys, labels);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                // TODO なんで抜いたっけ？のコメントを入れる。GOTOはforがらみでifeqと二重カウントされたから一旦退けたっぽい https://github.com/dddjava/jig/issues/320 けど、JSRは不明。
                if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR) {
                    // 何かしらの分岐がある
                    methodInstructions.register(MethodInstructionType.JUMP);
                }

                if (opcode == Opcodes.IFNONNULL || opcode == Opcodes.IFNULL) {
                    methodInstructions.register(MethodInstructionType.NULL判定);
                }
                super.visitJumpInsn(opcode, label);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
            }
        };
    }

    /**
     * Visibilityに持っていきたいが、accessの定数はasmが持っているのでここに置いておく。
     * 実際はバイトコードの固定値。
     * <p>
     * classの場合、ソースコードではpublic,protected,default,privateは定義できるが、
     * バイトコードではpublicか否かしか識別できない。
     * さらにprotectedもpublicになる。（パッケージ外から参照可能なので。）
     */
    private TypeVisibility resolveVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return TypeVisibility.PUBLIC;
        return TypeVisibility.NOT_PUBLIC;
    }

    private Visibility resolveMethodVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return Visibility.PUBLIC;
        if ((access & Opcodes.ACC_PROTECTED) != 0) return Visibility.PROTECTED;
        if ((access & Opcodes.ACC_PRIVATE) != 0) return Visibility.PRIVATE;
        return Visibility.PACKAGE;
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

    private TypeIdentifier typeDescriptorToIdentifier(String descriptor) {
        Type type = Type.getType(descriptor);
        return toTypeIdentifier(type);
    }

    private static TypeIdentifier toTypeIdentifier(Type type) {
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
                                    collector[0] = TypeIdentifier.valueOf(name);
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
                                            collector[1] = TypeIdentifier.valueOf(name);
                                        }
                                    };
                                }
                            };
                        }
                    }
            );
        }

        boolean 戻り値型が解決できない = collector[0] == null;
        boolean 型引数がバインドされていない = collector[1] == null;
        if (戻り値型が解決できない || 型引数がバインドされていない) {
            // signatureではなくdescriptorから取得する
            TypeIdentifier returnTypeIdentifier = methodDescriptorToReturnIdentifier(descriptor);
            return MethodReturn.fromTypeOnly(returnTypeIdentifier);
        }

        return new MethodReturn(new ParameterizedType(collector[0], collector[1]));
    }

    private ParameterizedType superType(String superName, String signature) {
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature == null) {
            return new ParameterizedType(TypeIdentifier.valueOf(superName));
        }

        SignatureVisitor noOpVisitor = new SignatureVisitor(this.api) {
        };

        List<TypeIdentifier> typeParameters = new ArrayList<>();

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
                                            typeParameters.add(TypeIdentifier.valueOf(name));
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

        return ParameterizedType.convert(TypeIdentifier.valueOf(superName), typeParameters);
    }

    private List<ParameterizedType> interfaceTypes(String[] interfaces, String signature) {
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature == null) {
            // 非総称型で作成
            return Arrays.stream(interfaces)
                    .map(TypeIdentifier::valueOf)
                    .map(ParameterizedType::new)
                    .collect(Collectors.toList());
        }

        SignatureVisitor noOpVisitor = new SignatureVisitor(this.api) {
        };

        List<ParameterizedType> parameterizedTypes = new ArrayList<>();
        new SignatureReader(signature).accept(
                new SignatureVisitor(this.api) {
                    @Override
                    public SignatureVisitor visitInterface() {

                        return new SignatureVisitor(this.api) {
                            final List<TypeIdentifier> typeParameters = new ArrayList<>();
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
                                            typeParameters.add(TypeIdentifier.valueOf(name));
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

        return parameterizedTypes;
    }

    public JigMethodBuilder createPlainMethodBuilder(JigTypeBuilder jigTypeBuilder,
                                                     int access,
                                                     Visibility visibility,
                                                     List<TypeIdentifier> signatureContainedTypes,
                                                     List<TypeIdentifier> throwsTypes,
                                                     Instructions instructions, MethodDeclaration methodDeclaration) {
        MethodDerivation methodDerivation = resolveMethodDerivation(methodDeclaration.methodSignature(), methodDeclaration.methodReturn(), access);
        var jigMethodBuilder = new JigMethodBuilder(methodDeclaration, signatureContainedTypes, visibility, methodDerivation, throwsTypes, instructions);

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
