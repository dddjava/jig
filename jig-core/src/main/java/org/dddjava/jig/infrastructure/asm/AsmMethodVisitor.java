package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.method.Visibility;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokeDynamicInstruction;
import org.dddjava.jig.domain.model.data.classes.method.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.data.classes.method.instruction.MethodInstructionType;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ( visitParameter )*
 * [ visitAnnotationDefault ]
 * ( visitAnnotation | visitAnnotableParameterCount | visitParameterAnnotation | visitTypeAnnotation | visitAttribute )*
 * [
 *   visitCode
 *   ( visitFrame | visit<i>X</i>Insn | visitLabel | visitInsnAnnotation | visitTryCatchBlock | visitTryCatchAnnotation | visitLocalVariable | visitLocalVariableAnnotation | visitLineNumber | visitAttribute )*
 *   visitMaxs
 * ]
 * visitEnd
 */
class AsmMethodVisitor extends MethodVisitor {

    private final Consumer<AsmMethodVisitor> endConsumer;

    // visitMethod由来の情報
    final MethodDeclaration methodDeclaration;
    final Visibility visibility;
    final List<TypeIdentifier> throwsTypes;
    // このVisitorで収集した情報
    final Instructions methodInstructions;
    final List<Annotation> annotationList;

    public AsmMethodVisitor(int api, Visibility visibility, List<TypeIdentifier> throwsTypes, MethodDeclaration methodDeclaration, Consumer<AsmMethodVisitor> endConsumer) {
        super(api);
        this.visibility = visibility;
        this.throwsTypes = throwsTypes;
        this.methodDeclaration = methodDeclaration;
        this.methodInstructions = Instructions.newInstance();
        this.annotationList = new ArrayList<>();
        this.endConsumer = endConsumer;
    }

    public static MethodVisitor from(int api,
                                     // visitMethodの引数
                                     int access, String name, String descriptor, String signature, String[] exceptions,
                                     TypeIdentifier typeIdentifier, Consumer<AsmMethodVisitor> endConsumer) {
        MethodDeclaration methodDeclaration = Optional.ofNullable(signature)
                .flatMap(nonNullSignature ->
                        // signatureがあればこちらから構築する
                        AsmMethodSignatureVisitor.buildMethodDeclaration(api, name, typeIdentifier, nonNullSignature)
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
                    return new MethodDeclaration(typeIdentifier, methodSignature, methodReturn);
                });

        List<TypeIdentifier> throwsTypes = Optional.ofNullable(exceptions).stream()
                .flatMap(Arrays::stream)
                .map(TypeIdentifier::valueOf)
                .toList();

        return new AsmMethodVisitor(api,
                resolveMethodVisibility(access),
                throwsTypes,
                methodDeclaration,
                endConsumer);
    }

    private static Visibility resolveMethodVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return Visibility.PUBLIC;
        if ((access & Opcodes.ACC_PROTECTED) != 0) return Visibility.PROTECTED;
        if ((access & Opcodes.ACC_PRIVATE) != 0) return Visibility.PRIVATE;
        return Visibility.PACKAGE;
    }

    static TypeIdentifier methodDescriptorToReturnIdentifier(String descriptor) {
        return AsmClassVisitor.toTypeIdentifier(Type.getReturnType(descriptor));
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
                annotation -> annotationList.add(annotation));
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
            var typeIdentifier = AsmClassVisitor.toTypeIdentifier(typeValue);
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
                            .map(AsmClassVisitor::toTypeIdentifier)
                            .collect(Collectors.toList());
                    var handleReturnType = methodDescriptorToReturnIdentifier(handle.getDesc());
                    var handleInvokeMethod = new InvokedMethod(handleOwnerType, handleMethodName, handleArgumentTypes, handleReturnType);

                    var returnType = AsmClassVisitor.toTypeIdentifier(type.getReturnType());
                    var argumentTypes = Arrays.stream(type.getArgumentTypes()).map(t -> AsmClassVisitor.toTypeIdentifier(t)).toList();

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
