package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.models.jigobject.class_.TypeKind;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.parts.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.field.FieldType;
import org.dddjava.jig.domain.model.parts.classes.method.*;
import org.dddjava.jig.domain.model.parts.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeArgumentList;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSource;
import org.dddjava.jig.domain.model.sources.jigfactory.JigMethodBuilder;
import org.dddjava.jig.domain.model.sources.jigfactory.JigTypeBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class AsmClassVisitor extends ClassVisitor {
    static Logger logger = LoggerFactory.getLogger(AsmClassVisitor.class);

    private final ClassSource classSource;
    private JigTypeBuilder jigTypeBuilder;

    AsmClassVisitor(ClassSource classSource) {
        super(Opcodes.ASM9);
        this.classSource = classSource;
    }

    @Override
    public void visitEnd() {
        logger.debug("visit {} end", classSource);
        super.visitEnd();
    }

    public JigTypeBuilder jigTypeBuilder() {
        // visitEnd後にしか呼んではいけない
        return Objects.requireNonNull(jigTypeBuilder);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        List<TypeIdentifier> actualTypeParameters = extractClassTypeFromGenericsSignature(signature);

        jigTypeBuilder = JigTypeBuilder.constructWithHeaders(
                new ParameterizedType(TypeIdentifier.valueOf(name), actualTypeParameters),
                superType(superName, signature),
                interfaceTypes(interfaces, signature),
                resolveVisibility(access),
                typeKind(access)
        );

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

        if ((access & Opcodes.ACC_RECORD) != 0) {
            return TypeKind.レコード型;
        }

        return TypeKind.通常型;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new MyAnnotationVisitor(this.api, typeDescriptorToIdentifier(descriptor), annotation ->
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
                    return new MyAnnotationVisitor(this.api, annotationTypeIdentifier,
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

        MethodReturn methodReturn = extractParameterizedReturnType(signature, descriptor);
        List<TypeIdentifier> useTypes = extractClassTypeFromGenericsSignature(signature);

        List<TypeIdentifier> throwsTypes = new ArrayList<>();
        if (exceptions != null) {
            for (String exception : exceptions) {
                throwsTypes.add(TypeIdentifier.valueOf(exception));
            }
        }

        JigMethodBuilder jigMethodBuilder = createPlainMethodBuilder(
                jigTypeBuilder,
                toMethodSignature(name, descriptor),
                methodReturn,
                access,
                resolveMethodVisibility(access),
                useTypes,
                throwsTypes);

        return new MethodVisitor(this.api) {

            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.ACONST_NULL) {
                    jigMethodBuilder.markReferenceNull();
                }
                super.visitInsn(opcode);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return new MyAnnotationVisitor(this.api, typeDescriptorToIdentifier(descriptor),
                        annotation -> jigMethodBuilder.addAnnotation(annotation));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                TypeIdentifier declaringType = TypeIdentifier.valueOf(owner);
                FieldType fieldType = typeDescriptorToFieldType(descriptor);
                FieldDeclaration fieldDeclaration = new FieldDeclaration(declaringType, fieldType, name);

                // FIXME: これをFieldDeclarationで扱うとFieldTypeに総称型が入っているのを期待しかねない
                // このメソッドのdescriptorではフィールドの型パラメタが解決できないため、完全なFieldTypeを作成できない。
                // UsingFieldでではFieldDeclarationと異なる形式で扱わなければならない。
                jigMethodBuilder.addFieldInstruction(fieldDeclaration);

                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                jigMethodBuilder.addMethodInstruction(toMethodDeclaration(owner, name, descriptor));

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitLdcInsn(Object value) {
                if (value instanceof Type) {
                    // `Xxx.class` などのクラス参照を読み込む
                    jigMethodBuilder.addClassReferenceCall(toTypeIdentifier((Type) value));
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
                //
                // Lambdaやメソッド参照のみを処理するために以下の条件を入れる方が良いかもしれない。
                // if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner())) {

                for (Object bootstrapMethodArgument : bootstrapMethodArguments) {

                    if (bootstrapMethodArgument instanceof Type type) {
                        if (type.getSort() == Type.METHOD) {
                            // lambdaやメソッドリファレンスの引数と戻り値型を読み込むつもりの実装。
                            // メソッドのシグネチャにあるし、他のものも扱ってしまっているので、確認して削除してしまいたい。
                            jigMethodBuilder.addInvokeDynamicType(toTypeIdentifier(type.getReturnType()));
                            for (Type argumentType : type.getArgumentTypes()) {
                                jigMethodBuilder.addInvokeDynamicType(toTypeIdentifier(argumentType));
                            }
                        }
                    }

                    if (bootstrapMethodArgument instanceof Handle handle) {
                        if (isMethodRef(handle)) {
                            var methodDeclaration = toMethodDeclaration(handle.getOwner(), handle.getName(), handle.getDesc());
                            jigMethodBuilder.addMethodInstruction(methodDeclaration);
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

            private MethodDeclaration toMethodDeclaration(String owner, String name, String desc) {
                return new MethodDeclaration(TypeIdentifier.valueOf(owner), toMethodSignature(name, desc), new MethodReturn(methodDescriptorToReturnIdentifier(desc)));
            }

            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                // switchがある
                jigMethodBuilder.addLookupSwitch();
                super.visitLookupSwitchInsn(dflt, keys, labels);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR) {
                    // 何かしらの分岐がある
                    jigMethodBuilder.addJump();
                }

                if (opcode == Opcodes.IFNONNULL || opcode == Opcodes.IFNULL) {
                    jigMethodBuilder.markJudgeNull();
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
     *
     * classの場合、ソースコードではpublic,protected,default,privateは定義できるが、
     * バイトコードではpublicか否かしか識別できない。
     * さらにprotectedもpublicになる。（パッケージ外から参照可能なので。）
     */
    private Visibility resolveVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return Visibility.PUBLIC;
        return Visibility.NOT_PUBLIC;
    }

    private Visibility resolveMethodVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return Visibility.PUBLIC;
        if ((access & Opcodes.ACC_PROTECTED) != 0) return Visibility.PROTECTED;
        if ((access & Opcodes.ACC_PRIVATE) != 0) return Visibility.PRIVATE;
        return Visibility.PACKAGE;
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

    private TypeIdentifier toTypeIdentifier(Type type) {
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
            return new MethodReturn(returnTypeIdentifier);
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

        return new ParameterizedType(TypeIdentifier.valueOf(superName), new TypeArgumentList(typeParameters));
    }

    private List<ParameterizedType> interfaceTypes(String[] interfaces, String signature) {
        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature == null) {
            // 非総称型で作成
            List<ParameterizedType> list = Arrays.stream(interfaces)
                    .map(TypeIdentifier::valueOf)
                    .map(ParameterizedType::new)
                    .collect(Collectors.toList());
            return list;
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
                                parameterizedTypes.add(new ParameterizedType(TypeIdentifier.valueOf(interfaceName), new TypeArgumentList(typeParameters)));
                            }
                        };
                    }
                }
        );

        return parameterizedTypes;
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
                final List<Object> list = new ArrayList<>();

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


    public JigMethodBuilder createPlainMethodBuilder(JigTypeBuilder jigTypeBuilder, MethodSignature methodSignature,
                                                     MethodReturn methodReturn,
                                                     int access,
                                                     Visibility visibility,
                                                     List<TypeIdentifier> useTypes,
                                                     List<TypeIdentifier> throwsTypes) {
        MethodDeclaration methodDeclaration = new MethodDeclaration(jigTypeBuilder.typeIdentifier(), methodSignature, methodReturn);
        MethodDerivation methodDerivation = resolveMethodDerivation(methodSignature, methodReturn, access);
        var jigMethodBuilder = JigMethodBuilder.constructWithHeader(methodDeclaration, useTypes, visibility, throwsTypes, methodDerivation);

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
