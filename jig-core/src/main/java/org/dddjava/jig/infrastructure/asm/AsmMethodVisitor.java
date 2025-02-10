package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokeDynamicInstruction;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.data.classes.method.instruction.MethodInstructionType;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.*;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.dddjava.jig.domain.model.sources.classsources.JigMethodBuilder;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(AsmMethodVisitor.class);

    private final Consumer<AsmMethodVisitor> endConsumer;

    // このVisitorで収集した情報
    final Instructions methodInstructions = Instructions.newInstance();
    private final ArrayList<JigAnnotationReference> declarationAnnotationCollector = new ArrayList<>();

    private AsmMethodVisitor(int api, Consumer<AsmMethodVisitor> endConsumer) {
        super(api);
        this.endConsumer = endConsumer;
    }

    public static MethodVisitor from(int api,
                                     // visitMethodの引数
                                     int access, String name, String descriptor, String signature, String[] exceptions,
                                     TypeIdentifier declaringTypeIdentifier, boolean isEnum, JigMemberBuilder jigMemberBuilder) {
        MethodDeclaration methodDeclaration = Optional.ofNullable(signature)
                .flatMap(nonNullSignature ->
                        // signatureがあればこちらから構築する
                        AsmMethodSignatureVisitor.buildMethodDeclaration(api, name, declaringTypeIdentifier, nonNullSignature)
                ).orElseGet(() -> {
                    // signatureがないもしくは失敗した場合はdescriptorから構築する
                    // signatureの解析失敗はともかく、descriptorしかない場合はこの生成で適切なMethodSignatureができる

                    // descriptorから戻り値型を生成
                    MethodReturn methodReturn = MethodReturn.fromTypeOnly(methodDescriptorToReturnIdentifier(descriptor));
                    // descriptorから引数型を生成
                    List<ParameterizedType> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                            .map(type -> asmType2TypeIdentifier(type))
                            .map(ParameterizedType::noneGenerics)
                            .collect(Collectors.toList());
                    var methodSignature = MethodSignature.from(name, argumentTypes);
                    return new MethodDeclaration(declaringTypeIdentifier, methodSignature, methodReturn);
                });

        // これもsignatureがあればsignatureからとれるけれど、Throwableはジェネリクスにできないしexceptionsだけで十分そう
        // throwsのアノテーションが必要になったら別途考える
        var throwsList = Optional.ofNullable(exceptions).stream().flatMap(Arrays::stream)
                .map(JigTypeReference::fromJvmBinaryName).toList();

        var methodType = Type.getMethodType(descriptor);
        // idはsignature有無に関わらずdeclaringType,name,descriptorから作る
        var jigMethodIdentifier = JigMethodIdentifier.from(declaringTypeIdentifier, name,
                Arrays.stream(methodType.getArgumentTypes()).map(type -> asmType2TypeIdentifier(type)).toList());

        return new AsmMethodVisitor(api,
                it -> {
                    JigMethodHeader jigMethodHeader = it.jigMethodHeader(access, signature, jigMethodIdentifier, methodType, throwsList);
                    JigMethodDeclaration jigMethodDeclaration = new JigMethodDeclaration(jigMethodHeader, it.methodInstructions);
                    jigMemberBuilder.addJigMethodDeclaration(jigMethodDeclaration);
                    JigMethodBuilder jigMethodBuilder = JigMethodBuilder.builder(
                            jigMethodDeclaration,
                            access,
                            methodDeclaration,
                            isEnum,
                            jigMemberBuilder.isRecordComponent(methodDeclaration));

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
                }
        );
    }

    private JigMethodHeader jigMethodHeader(int access, String signature, JigMethodIdentifier jigMethodIdentifier, Type methodType, List<JigTypeReference> throwsList) {
        if (signature != null) {
            var methodSignatureVisitor = AsmMethodSignatureVisitor.buildMethodSignatureVisitor(api, signature);
            var jigTypeReference = methodSignatureVisitor.returnVisitor.jigTypeReference();
            var parameters = methodSignatureVisitor.parameterVisitors.stream().map(visitor -> visitor.jigTypeReference()).toList();
            return jigMethodHeader(access, jigMethodIdentifier, jigTypeReference, parameters, throwsList);
        }

        return jigMethodHeader(access, jigMethodIdentifier, JigTypeReference.fromId(asmType2TypeIdentifier(methodType.getReturnType())), Arrays.stream(methodType.getArgumentTypes())
                .map(AsmMethodVisitor::asmType2TypeIdentifier)
                .map(JigTypeReference::fromId)
                .toList(), throwsList);
    }

    private JigMethodHeader jigMethodHeader(int access, JigMethodIdentifier jigMethodIdentifier, JigTypeReference returnType, List<JigTypeReference> parameterList, List<JigTypeReference> throwsList) {

        EnumSet<JigMethodFlag> flags = EnumSet.noneOf(JigMethodFlag.class);
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) flags.add(JigMethodFlag.SYNCHRONIZED);
        if ((access & Opcodes.ACC_BRIDGE) != 0) flags.add(JigMethodFlag.BRIDGE);
        if ((access & Opcodes.ACC_VARARGS) != 0) flags.add(JigMethodFlag.VARARGS);
        if ((access & Opcodes.ACC_NATIVE) != 0) flags.add(JigMethodFlag.NATIVE);
        if ((access & Opcodes.ACC_ABSTRACT) != 0) flags.add(JigMethodFlag.ABSTRACT);
        if ((access & Opcodes.ACC_STRICT) != 0) flags.add(JigMethodFlag.STRICT);
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) flags.add(JigMethodFlag.SYNTHETIC);

        return new JigMethodHeader(jigMethodIdentifier, AsmUtils.jigMemberOwnership(access),
                new JigMethodAttribute(AsmUtils.resolveMethodVisibility(access), declarationAnnotationCollector, returnType, parameterList, throwsList, flags)
        );
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        logger.debug("visitAnnotation {} {}", descriptor, visible);
        return new AsmAnnotationVisitor(this.api, AsmUtils.typeDescriptorToIdentifier(descriptor), it -> {
            declarationAnnotationCollector.add(it.annotationReference());
        });
    }

    @Override
    public void visitInsn(int opcode) {
        logger.debug("visitInsn {}", opcode);
        if (opcode == Opcodes.ACONST_NULL) {
            methodInstructions.register(MethodInstructionType.NULL参照);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        logger.debug("visitFieldInsn {} {} {} {}", opcode, owner, name, descriptor);
        TypeIdentifier declaringType = TypeIdentifier.valueOf(owner);
        TypeIdentifier fieldTypeIdentifier = AsmUtils.typeDescriptorToIdentifier(descriptor);

        methodInstructions.registerField(declaringType, fieldTypeIdentifier, name);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        logger.debug("visitMethodInsn {} {} {} {}", opcode, owner, name, descriptor);
        List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(type -> asmType2TypeIdentifier(type))
                .toList();
        TypeIdentifier returnType = methodDescriptorToReturnIdentifier(descriptor);

        InvokedMethod invokedMethod = new InvokedMethod(TypeIdentifier.valueOf(owner), name, argumentTypes, returnType);
        methodInstructions.registerMethod(invokedMethod);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitLdcInsn(Object value) {
        logger.debug("visitLdcInsn {}", value);
        if (value instanceof Type typeValue) {
            // `Xxx.class` などのクラス参照を読み込む
            var typeIdentifier = asmType2TypeIdentifier(typeValue);
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
        logger.debug("visitInvokeDynamicInsn {} {} {} {}", name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
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
                logger.warn("想定外のInvokeDynamicが検出されました。読み飛ばします。 {} {}", name, descriptor);
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
                            .map(type1 -> asmType2TypeIdentifier(type1))
                            .collect(Collectors.toList());
                    var handleReturnType = methodDescriptorToReturnIdentifier(handle.getDesc());
                    var handleInvokeMethod = new InvokedMethod(handleOwnerType, handleMethodName, handleArgumentTypes, handleReturnType);

                    var returnType = asmType2TypeIdentifier(type.getReturnType());
                    var argumentTypes = Arrays.stream(type.getArgumentTypes()).map(t -> asmType2TypeIdentifier(t)).toList();

                    methodInstructions.registerInvokeDynamic(new InvokeDynamicInstruction(handleInvokeMethod, returnType, argumentTypes));
                }
            }
        }

        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        logger.debug("visitLookupSwitchInsn {} {} {}", dflt, keys, labels);
        // switchがある
        methodInstructions.register(MethodInstructionType.SWITCH);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        logger.debug("visitJumpInsn {} {}", opcode, label);
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
        logger.debug("visitEnd {}", this);
        endConsumer.accept(this);
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

    private static TypeIdentifier asmType2TypeIdentifier(Type type) {
        return TypeIdentifier.valueOf(type.getClassName());
    }

    private static TypeIdentifier methodDescriptorToReturnIdentifier(String descriptor) {
        return asmType2TypeIdentifier(Type.getReturnType(descriptor));
    }
}
