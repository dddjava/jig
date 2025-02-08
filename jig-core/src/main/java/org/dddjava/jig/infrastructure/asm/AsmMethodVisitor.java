package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokeDynamicInstruction;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.data.classes.method.instruction.MethodInstructionType;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.*;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.dddjava.jig.domain.model.sources.classsources.JigMethodBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ( visitParameter )*
 * [ visitAnnotationDefault ]
 * ( visitAnnotation | visitAnnotableParameterCount | visitParameterAnnotation | visitTypeAnnotation | visitAttribute )*
 * [
 * visitCode
 * ( visitFrame | visit<i>X</i>Insn | visitLabel | visitInsnAnnotation | visitTryCatchBlock | visitTryCatchAnnotation | visitLocalVariable | visitLocalVariableAnnotation | visitLineNumber | visitAttribute )*
 * visitMaxs
 * ]
 * visitEnd
 */
class AsmMethodVisitor extends MethodVisitor {

    private final Consumer<AsmMethodVisitor> endConsumer;

    // visitMethod由来の情報
    final JigMethodHeader jigMethodHeader;
    final MethodDeclaration methodDeclaration;
    // このVisitorで収集した情報
    final Instructions methodInstructions;
    final List<Annotation> annotationList;
    final List<TypeIdentifier> signatureContainedTypes;

    public AsmMethodVisitor(int api, JigMethodHeader jigMethodHeader, MethodDeclaration methodDeclaration, Consumer<AsmMethodVisitor> endConsumer, List<TypeIdentifier> signatureContainedTypes) {
        super(api);
        this.jigMethodHeader = jigMethodHeader;
        this.methodDeclaration = methodDeclaration;
        this.signatureContainedTypes = signatureContainedTypes;
        this.methodInstructions = Instructions.newInstance();
        this.annotationList = new ArrayList<>();
        this.endConsumer = endConsumer;
    }

    public static MethodVisitor from(int api,
                                     // visitMethodの引数
                                     int access, String name, String descriptor, String signature, String[] exceptions,
                                     TypeIdentifier typeIdentifier, boolean isEnum, JigMemberBuilder jigMemberBuilder) {
        List<TypeIdentifier> signatureContainedTypes = new ArrayList<>();
        if (signature != null) {
            // シグネチャに登場する型を全部取り出す
            new SignatureReader(signature).accept(
                    new SignatureVisitor(api) {
                        @Override
                        public void visitClassType(String name1) {
                            signatureContainedTypes.add(TypeIdentifier.valueOf(name1));
                        }
                    }
            );
        }

        MethodDeclaration methodDeclaration = Optional.ofNullable(signature)
                .flatMap(nonNullSignature ->
                        // signatureがあればこちらから構築する
                        // TODO MethodDeclarationだけ取得しているがsignatureから取得できる型の収集を別でやっているため、
                        //  methodSignatureを2回読み取るようになっている。この１か所だけで読むように寄せる。（useTypesもやめたい）
                        AsmMethodSignatureVisitor.buildMethodDeclaration(api, name, typeIdentifier, nonNullSignature)
                ).orElseGet(() -> {
                    // signatureがないもしくは失敗した場合はdescriptorから構築する
                    // signatureの解析失敗はともかく、descriptorしかない場合はこの生成で適切なMethodSignatureができる

                    // descriptorから戻り値型を生成
                    MethodReturn methodReturn = MethodReturn.fromTypeOnly(methodDescriptorToReturnIdentifier(descriptor));
                    // descriptorから引数型を生成
                    List<ParameterizedType> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                            .map(type -> TypeIdentifier.valueOf(type.getClassName()))
                            .map(ParameterizedType::noneGenerics)
                            .collect(Collectors.toList());
                    var methodSignature = MethodSignature.from(name, argumentTypes);
                    return new MethodDeclaration(typeIdentifier, methodSignature, methodReturn);
                });

        // これもsignatureがあればsignatureからとれるけれど、Throwableはジェネリクスにできないしexceptionsだけで十分そう
        List<TypeIdentifier> throwsTypes = Optional.ofNullable(exceptions).stream()
                .flatMap(Arrays::stream)
                .map(TypeIdentifier::valueOf)
                .toList();

        var methodType = Type.getMethodType(descriptor);
        var jigMethodIdentifier = JigMethodIdentifier.from(typeIdentifier, name, Arrays.stream(methodType.getArgumentTypes()).map(type -> TypeIdentifier.valueOf(type.getClassName())).toList());
        var methodReturn = methodDeclaration.methodReturn().parameterizedType();
        var argumentList = methodDeclaration.methodSignature().arguments().stream()
                .map(it -> parameterizedTypeToTypeReference(it))
                .toList();
        var jigMethodHeader = new JigMethodHeader(
                jigMethodIdentifier,
                // fieldと同じ判定をしているので共通化したい
                ((access & Opcodes.ACC_STATIC) == 0) ? JigMemberOwnership.INSTANCE : JigMemberOwnership.CLASS,
                new JigMethodAttribute(
                        resolveMethodVisibility(access),
                        List.of(), // あのてーしょん未対応
                        parameterizedTypeToTypeReference(methodReturn),
                        argumentList,
                        throwsTypes.stream().map(JigTypeReference::fromId).toList(),
                        jigMethodFlags(access)
                )
        );

        return new AsmMethodVisitor(api,
                jigMethodHeader,
                methodDeclaration,
                it -> {
                    JigMethodBuilder jigMethodBuilder = JigMethodBuilder.builder(
                            it.jigMethodHeader,
                            access,
                            it.signatureContainedTypes,
                            it.methodDeclaration,
                            it.annotationList,
                            it.methodInstructions,
                            isEnum,
                            jigMemberBuilder.isRecordComponent(it.methodDeclaration));

                    if (jigMethodBuilder.methodIdentifier().methodSignature().isConstructor()) {
                        // コンストラクタ
                        jigMemberBuilder.addConstructor(jigMethodBuilder);
                    } else if ((access & Opcodes.ACC_STATIC) != 0) {
                        // staticメソッド
                        jigMemberBuilder.addStaticMethod(jigMethodBuilder);
                    } else {
                        // コンストラクタでもstaticメソッドでもない＝インスタンスメソッド
                        jigMemberBuilder.addInstanceMethod(jigMethodBuilder);
                    }
                },
                signatureContainedTypes);
    }

    private static EnumSet<JigMethodFlag> jigMethodFlags(int access) {
        EnumSet<JigMethodFlag> set = EnumSet.noneOf(JigMethodFlag.class);
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) set.add(JigMethodFlag.SYNCHRONIZED);
        if ((access & Opcodes.ACC_BRIDGE) != 0) set.add(JigMethodFlag.BRIDGE);
        if ((access & Opcodes.ACC_VARARGS) != 0) set.add(JigMethodFlag.VARARGS);
        if ((access & Opcodes.ACC_NATIVE) != 0) set.add(JigMethodFlag.NATIVE);
        if ((access & Opcodes.ACC_ABSTRACT) != 0) set.add(JigMethodFlag.ABSTRACT);
        if ((access & Opcodes.ACC_STRICT) != 0) set.add(JigMethodFlag.STRICT);
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) set.add(JigMethodFlag.SYNTHETIC);

        return set;
    }

    private static JigTypeReference parameterizedTypeToTypeReference(ParameterizedType parameterizedType) {
        return new JigTypeReference(
                parameterizedType.typeIdentifier(),
                List.of(), // type annotation未対応
                parameterizedType.typeParameters().list().stream()
                        .map(typeParameter -> new JigTypeArgument(typeParameter.value()))
                        .toList()
        );
    }

    private static JigMemberVisibility resolveMethodVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return JigMemberVisibility.PUBLIC;
        if ((access & Opcodes.ACC_PROTECTED) != 0) return JigMemberVisibility.PROTECTED;
        if ((access & Opcodes.ACC_PRIVATE) != 0) return JigMemberVisibility.PRIVATE;
        return JigMemberVisibility.PACKAGE;
    }

    private static TypeIdentifier methodDescriptorToReturnIdentifier(String descriptor) {
        return TypeIdentifier.valueOf(Type.getReturnType(descriptor).getClassName());
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ACONST_NULL) {
            methodInstructions.register(MethodInstructionType.NULL参照);
        }
        super.visitInsn(opcode);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AsmAnnotationVisitor(this.api,
                AsmClassVisitor.typeDescriptorToIdentifier(descriptor),
                it -> annotationList.add(new Annotation(it.annotationType, it.annotationDescription)));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        TypeIdentifier declaringType = TypeIdentifier.valueOf(owner);
        TypeIdentifier fieldTypeIdentifier = AsmClassVisitor.typeDescriptorToIdentifier(descriptor);

        methodInstructions.registerField(declaringType, fieldTypeIdentifier, name);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(type -> TypeIdentifier.valueOf(type.getClassName()))
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
            var typeIdentifier = TypeIdentifier.valueOf(typeValue.getClassName());
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
                AsmClassVisitor.logger.warn("想定外のInvokeDynamicが {} で検出されました。読み飛ばします。", methodDeclaration);
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
                            .map(type1 -> TypeIdentifier.valueOf(type1.getClassName()))
                            .collect(Collectors.toList());
                    var handleReturnType = methodDescriptorToReturnIdentifier(handle.getDesc());
                    var handleInvokeMethod = new InvokedMethod(handleOwnerType, handleMethodName, handleArgumentTypes, handleReturnType);

                    var returnType = TypeIdentifier.valueOf(type.getReturnType().getClassName());
                    var argumentTypes = Arrays.stream(type.getArgumentTypes()).map(t -> TypeIdentifier.valueOf(t.getClassName())).toList();

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
                AsmClassVisitor.logger.warn("予期しないHandler {} が検出されました。解析が部分的にスキップされます。このログが出力される場合、lambdaによるメソッド呼び出しが欠落する可能性があります。issueなどで再現コードをいただけると助かります。", handle);
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
        endConsumer.accept(this);
    }
}
