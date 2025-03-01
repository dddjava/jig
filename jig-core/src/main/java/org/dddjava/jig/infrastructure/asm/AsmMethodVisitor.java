package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.*;
import org.dddjava.jig.domain.model.data.members.instruction.*;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * メソッドのバイトコードから必要な情報を抽出するMethodVisitorの実装
 *
 * ```
 * ( visitParameter )*
 * [ visitAnnotationDefault ]
 * ( visitAnnotation | visitAnnotableParameterCount | visitParameterAnnotation | visitTypeAnnotation | visitAttribute )*
 * [
 * visitCode
 * ( visitFrame | visit<i>X</i>Insn | visitLabel | visitInsnAnnotation | visitTryCatchBlock | visitTryCatchAnnotation | visitLocalVariable | visitLocalVariableAnnotation | visitLineNumber | visitAttribute )*
 * visitMaxs
 * ]
 * visitEnd
 * ```
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.6">4.6. Methods</a>
 */
class AsmMethodVisitor extends MethodVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmMethodVisitor.class);

    private final ArrayList<Instruction> methodInstructionCollector = new ArrayList<>();
    private final ArrayList<JigAnnotationReference> declarationAnnotationCollector = new ArrayList<>();
    private final AsmClassVisitor contextClass;
    private final Consumer<AsmMethodVisitor> finisher;

    private AsmMethodVisitor(AsmClassVisitor contextClass, Consumer<AsmMethodVisitor> finisher) {
        super(contextClass.api());
        this.contextClass = contextClass;
        this.finisher = finisher;
    }

    public static MethodVisitor from(AsmClassVisitor contextClass, int access, String name, String descriptor, String signature, String[] exceptions) {
        // これもsignatureがあればsignatureからとれるけれど、Throwableはジェネリクスにできないしexceptionsだけで十分そう
        // throwsのアノテーションが必要になったら別途考える
        var throwsList = Optional.ofNullable(exceptions).stream().flatMap(Arrays::stream)
                .map(JigTypeReference::fromJvmBinaryName).toList();

        var methodType = Type.getMethodType(descriptor);
        // idはsignature有無に関わらずdeclaringType,name,descriptorから作る
        var jigMethodIdentifier = JigMethodIdentifier.from(contextClass.jigTypeHeader().id(), name,
                Arrays.stream(methodType.getArgumentTypes()).map(type -> asmType2TypeIdentifier(type)).toList());

        return new AsmMethodVisitor(contextClass,
                it -> {
                    JigMethodHeader jigMethodHeader = it.jigMethodHeader(access, signature, jigMethodIdentifier, methodType, throwsList);
                    contextClass.finishVisitMethod(jigMethodHeader, it.methodInstructionCollector);
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
        JigMemberOwnership ownership = AsmUtils.jigMemberOwnership(access);

        EnumSet<JigMethodFlag> flags = EnumSet.noneOf(JigMethodFlag.class);
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) flags.add(JigMethodFlag.SYNCHRONIZED);
        if ((access & Opcodes.ACC_BRIDGE) != 0) flags.add(JigMethodFlag.BRIDGE);
        if ((access & Opcodes.ACC_VARARGS) != 0) flags.add(JigMethodFlag.VARARGS);
        if ((access & Opcodes.ACC_NATIVE) != 0) flags.add(JigMethodFlag.NATIVE);
        if ((access & Opcodes.ACC_ABSTRACT) != 0) flags.add(JigMethodFlag.ABSTRACT);
        if ((access & Opcodes.ACC_STRICT) != 0) flags.add(JigMethodFlag.STRICT);
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) flags.add(JigMethodFlag.SYNTHETIC);

        String name = jigMethodIdentifier.name();
        if (name.equals("<init>")) flags.add(JigMethodFlag.INITIALIZER);
        if (name.equals("<clinit>")) flags.add(JigMethodFlag.STATIC_INITIALIZER);
        if (name.startsWith("lambda$")) flags.add(JigMethodFlag.LAMBDA_SUPPORT);

        contextClass.jigTypeHeader().baseTypeDataBundle().superType().ifPresent(superType -> {
            // enumの場合に生成される以下をわかるようにしておく
            // - public static MyEnum[] values();
            // - public static MyEnum valueOf(java.lang.String);
            // - private static MyEnum[] $values();
            if (superType.typeIs(Enum.class)) {
                if (ownership == JigMemberOwnership.CLASS) {
                    if ((name.equals("values") && parameterList.isEmpty())
                            || (name.equals("$values()") && parameterList.isEmpty())
                            || (name.equals("valueOf") && parameterList.size() == 1 && parameterList.get(0).typeIs(String.class))) {
                        flags.add(JigMethodFlag.ENUM_SUPPORT);
                    }
                }
            }
            // recordの場合にcomponentをわかるようにしておく
            if (superType.typeIs(Record.class)) {
                if (parameterList.isEmpty() && contextClass.isRecordComponentName(jigMethodIdentifier.name())) {
                    flags.add(JigMethodFlag.RECORD_COMPONENT_ACCESSOR);
                }
            }
        });

        return new JigMethodHeader(jigMethodIdentifier, ownership,
                new JigMethodAttribute(AsmUtils.resolveMethodVisibility(access), declarationAnnotationCollector, returnType, parameterList, throwsList, flags)
        );
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        logger.debug("visitAnnotation {} {}", descriptor, visible);
        return AsmAnnotationVisitor.from(this.api, AsmUtils.typeDescriptorToIdentifier(descriptor), it -> {
            declarationAnnotationCollector.add(it.annotationReference());
        });
    }

    @Override
    public void visitInsn(int opcode) {
        logger.debug("visitInsn {}", opcode);
        if (opcode == Opcodes.ACONST_NULL) {
            methodInstructionCollector.add(BasicInstruction.NULL参照);
        }
        super.visitInsn(opcode);
    }

    /**
     * フィールドに関するinstruction
     * descriptorはフィールドの型だが、対象のフィールドの型自体にそれほど重要な意味はないので、
     * JIGでは使用しないことにする。
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        logger.debug("visitFieldInsn {} {} {} {}", opcode, owner, name, descriptor);
        TypeIdentifier declaringType = TypeIdentifier.valueOf(owner);

        JigFieldIdentifier jigFieldIdentifier = JigFieldIdentifier.from(declaringType, name);
        var fieldInstruction = switch (opcode) {
            case Opcodes.H_GETFIELD, Opcodes.H_GETSTATIC -> FieldAccess.get(jigFieldIdentifier);
            case Opcodes.H_PUTFIELD, Opcodes.H_PUTSTATIC -> FieldAccess.set(jigFieldIdentifier);
            // エラーにせず、ASMがFieldInsnを検出したことだけは記録しておく
            default -> FieldAccess.unknown(jigFieldIdentifier);
        };

        methodInstructionCollector.add(fieldInstruction);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        logger.debug("visitMethodInsn {} {} {} {}", opcode, owner, name, descriptor);
        List<TypeIdentifier> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(type -> asmType2TypeIdentifier(type))
                .toList();
        TypeIdentifier returnType = methodDescriptorToReturnIdentifier(descriptor);

        MethodCall methodCall = new MethodCall(TypeIdentifier.valueOf(owner), name, argumentTypes, returnType);
        methodInstructionCollector.add(methodCall);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitLdcInsn(Object value) {
        logger.debug("visitLdcInsn {}", value);
        if (value instanceof Type typeValue) {
            // `Xxx.class` などのクラス参照を読み込む
            var typeIdentifier = asmType2TypeIdentifier(typeValue);
            methodInstructionCollector.add(new ClassReference(typeIdentifier));
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
        // たとえばFunctionで受けるなら name=apply descriptor=()Ljava/util/valueResolver/Function; となる。
        // invokeDynamic実行時点でのこの情報あまり意味がないので使用しない。（必要であれば他のメソッド呼び出し時の引数として登場するはず。）

        // bootstrapMethodHandleはinvokedynamicの起動メソッドが入る。
        // Lambdaの場合は LambdaMetafactory#metafactory になり、他にも以下のようなものがある。
        // - 文字列の+での連結: StringConcatFactory#makeConcatWithConstants
        // - recordのtoStringなど: ObjectMethods#bootstrap
        // これ自体はアプリケーションコード実装者が意識するものでないので、当面JIGではスルーする。

        // JavaでのLambdaやメソッド参照のみを処理する
        if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner())
                && "metafactory".equals(bootstrapMethodHandle.getName())) {
            if ((bootstrapMethodArguments.length == 3)
                    // 0: Type 実装時の型。ジェネリクスなどは無視されるため、Functionの場合は (LObject;)LObject; となる。
                    // 1: Handle: メソッド参照の場合は対象のメソッドのシグネチャ、Lambda式の場合は生成されたLambdaのシグネチャ
                    // 2: Type 動的に適用される型。ジェネリクスなども解決される。Lambdaを受けるインタフェースがジェネリクスを使用していない場合は 0 と同じになる。
                    // 0は無視して1,2を参照する。
                    && (bootstrapMethodArguments[1] instanceof Handle handle && isMethodRef(handle)
                    && bootstrapMethodArguments[2] instanceof Type type && type.getSort() == Type.METHOD)) {
                // 実際に呼び出されるメソッド
                var handleOwnerType = TypeIdentifier.valueOf(handle.getOwner());
                var handleMethodName = handle.getName();
                var handleArgumentTypes = Arrays.stream(Type.getArgumentTypes(handle.getDesc()))
                        .map(type1 -> asmType2TypeIdentifier(type1))
                        .collect(Collectors.toList());
                var handleReturnType = methodDescriptorToReturnIdentifier(handle.getDesc());
                var handleMethodCall = new MethodCall(handleOwnerType, handleMethodName, handleArgumentTypes, handleReturnType);

                // returnType/argumentTypesは呼び出し側としての型。同じになることも多いが、違うこともある。
                // たとえば　int method() をメソッド参照で呼び出すと ()Ljava/lang/Integer; になったりする。
                var returnType = asmType2TypeIdentifier(type.getReturnType());
                var argumentTypes = Arrays.stream(type.getArgumentTypes()).map(AsmMethodVisitor::asmType2TypeIdentifier).toList();
                DynamicMethodCall dynamicMethodCall = new DynamicMethodCall(handleMethodCall, returnType, argumentTypes);

                methodInstructionCollector.add(dynamicMethodCall);
            } else {
                logger.warn("JIGの想定していないLambdaMetafactory#metafactory使用が検出されました。読み飛ばします。 {} {}", name, descriptor);
            }
        }

        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        logger.debug("visitLookupSwitchInsn {} {} {}", dflt, keys, labels);
        // switchがある
        methodInstructionCollector.add(BasicInstruction.SWITCH);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        logger.debug("visitJumpInsn {} {}", opcode, label);
        // if<cond> はJumpInsnにくるのでこのメソッドで判定があるかを検出するが、
        // GOTOやJSR（Java7で削除されたJump to Subroutine。ASMに存在するので一応。）は
        // 判定せずの移動だけなので、「判定」の記録からは除外する。
        if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR) {
            if (opcode == Opcodes.IFNONNULL || opcode == Opcodes.IFNULL) {
                methodInstructionCollector.add(BasicInstruction.NULL判定);
            } else {
                methodInstructionCollector.add(BasicInstruction.判定);
            }
        }
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitEnd() {
        logger.debug("visitEnd {}", this);
        finisher.accept(this);
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
