package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.instruction.*;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodFlag;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

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
    private final ContextClass contextClass;
    private final Consumer<AsmMethodVisitor> finisher;

    private AsmMethodVisitor(ContextClass contextClass, Consumer<AsmMethodVisitor> finisher) {
        super(contextClass.api());
        this.contextClass = contextClass;
        this.finisher = finisher;
    }

    public static MethodVisitor from(ContextClass contextClass, int access, String name, String descriptor, @Nullable String signature, @Nullable String[] exceptions) {
        // これもsignatureがあればsignatureからとれるけれど、Throwableはジェネリクスにできないしexceptionsだけで十分そう
        // throwsのアノテーションが必要になったら別途考える
        var throwsList = Optional.ofNullable(exceptions).stream().flatMap(Arrays::stream)
                .map(JigTypeReference::fromJvmBinaryName).toList();

        var methodType = Type.getMethodType(descriptor);
        // idはsignature有無に関わらずdeclaringType,name,descriptorから作る
        var jigMethodId = JigMethodId.from(contextClass.typeId(), name,
                Arrays.stream(methodType.getArgumentTypes()).map(type -> AsmUtils.type2TypeId(type)).toList());

        return new AsmMethodVisitor(contextClass,
                it -> {
                    JigMethodHeader jigMethodHeader = it.jigMethodHeader(jigMethodId, access, methodType, throwsList, Optional.ofNullable(signature));
                    contextClass.finishVisitMethod(jigMethodHeader, it.methodInstructionCollector);
                }
        );
    }

    private JigMethodHeader jigMethodHeader(JigMethodId jigMethodId, int access, Type methodType, List<JigTypeReference> throwsList, Optional<String> optSignature) {
        return optSignature.map(signature -> {
            // signatureが使える場合はsignatureから型や引数を解釈する
            var methodSignatureVisitor = AsmMethodSignatureVisitor.readSignatureData(api, signature);
            var returnType = methodSignatureVisitor.returnType();
            var parameterTypeList = methodSignatureVisitor.parameterTypeList();
            return jigMethodHeader(jigMethodId, access, returnType, parameterTypeList, throwsList);
        }).orElseGet(() -> {
            var returnType = JigTypeReference.fromId(AsmUtils.type2TypeId(methodType.getReturnType()));
            var parameterTypeList = Arrays.stream(methodType.getArgumentTypes())
                    .map(type -> AsmUtils.type2TypeId(type))
                    .map(JigTypeReference::fromId)
                    .toList();
            return jigMethodHeader(jigMethodId, access, returnType, parameterTypeList, throwsList);
        });
    }

    private JigMethodHeader jigMethodHeader(JigMethodId jigMethodId, int access, JigTypeReference returnType, List<JigTypeReference> parameterList, List<JigTypeReference> throwsList) {
        var jigMemberVisibility = AsmUtils.resolveMethodVisibility(access);
        JigMemberOwnership ownership = AsmUtils.jigMemberOwnership(access);

        EnumSet<JigMethodFlag> flags = EnumSet.noneOf(JigMethodFlag.class);
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) flags.add(JigMethodFlag.SYNCHRONIZED);
        if ((access & Opcodes.ACC_BRIDGE) != 0) flags.add(JigMethodFlag.BRIDGE);
        if ((access & Opcodes.ACC_VARARGS) != 0) flags.add(JigMethodFlag.VARARGS);
        if ((access & Opcodes.ACC_NATIVE) != 0) flags.add(JigMethodFlag.NATIVE);
        if ((access & Opcodes.ACC_ABSTRACT) != 0) flags.add(JigMethodFlag.ABSTRACT);
        if ((access & Opcodes.ACC_STRICT) != 0) flags.add(JigMethodFlag.STRICT);
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) flags.add(JigMethodFlag.SYNTHETIC);

        String name = jigMethodId.name();
        if (name.equals("<init>")) flags.add(JigMethodFlag.INITIALIZER);
        if (name.equals("<clinit>")) flags.add(JigMethodFlag.STATIC_INITIALIZER);

        // lambda合成メソッドの判定
        // 名前だけ（lambda$ から始まる）は書こうと思えば書けるので、 ACC_PRIVATE, ACC_SYNTHETIC も条件とする。
        // ACC_STATICはlambdaでインスタンスメソッドを呼ばない場合に付与されるので条件にしてはいけない
        if (name.startsWith("lambda$")
                && jigMemberVisibility == JigMemberVisibility.PRIVATE
                && flags.contains(JigMethodFlag.SYNTHETIC)) {
            flags.add(JigMethodFlag.LAMBDA_SUPPORT);
        }

        if (contextClass.isEnum()) {
            // enumの場合に生成される以下をわかるようにしておく
            // - public static MyEnum[] values();
            // - public static MyEnum valueOf(java.lang.String);
            // - private static MyEnum[] $values();
            if (ownership == JigMemberOwnership.CLASS) {
                if ((name.equals("values") && parameterList.isEmpty())
                        || (name.equals("$values()") && parameterList.isEmpty())
                        || (name.equals("valueOf") && parameterList.size() == 1 && parameterList.get(0).id().equals(TypeId.STRING))) {
                    flags.add(JigMethodFlag.ENUM_SUPPORT);
                }
            }
        }
        if (contextClass.isRecord()) {
            // recordの場合にcomponentをわかるようにしておく
            if (parameterList.isEmpty() && contextClass.isRecordComponentName(jigMethodId.name())) {
                flags.add(JigMethodFlag.RECORD_COMPONENT_ACCESSOR);
            }
        }

        return JigMethodHeader.from(jigMethodId, ownership,
                jigMemberVisibility, declarationAnnotationCollector, returnType, parameterList, throwsList, flags);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        logger.debug("visitAnnotation {} {}", descriptor, visible);
        return AsmAnnotationVisitor.from(this.api, descriptor, it -> {
            declarationAnnotationCollector.add(it.annotationReference());
        });
    }

    @Override
    public void visitInsn(int opcode) {
        logger.debug("visitInsn {}", opcode);
        switch (opcode) {
            case Opcodes.ACONST_NULL -> methodInstructionCollector.add(SimpleInstruction.NULL参照);
            case Opcodes.RETURN, Opcodes.ARETURN,
                 Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN ->
                    methodInstructionCollector.add(SimpleInstruction.RETURN);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        logger.debug("visitFieldInsn {} {} {} {}", opcode, owner, name, descriptor);

        var fieldTypeId = AsmUtils.typeDescriptorToTypeId(descriptor);
        var declaringTypeId = TypeId.fromJvmBinaryName(owner);

        var jigFieldId = JigFieldId.from(declaringTypeId, name);
        var fieldInstruction = switch (opcode) {
            case Opcodes.GETFIELD, Opcodes.GETSTATIC -> FieldAccess.get(fieldTypeId, jigFieldId);
            case Opcodes.PUTFIELD, Opcodes.PUTSTATIC -> FieldAccess.set(fieldTypeId, jigFieldId);
            // エラーにせず、ASMがFieldInsnを検出したことだけは記録しておく
            default -> FieldAccess.unknown(fieldTypeId, jigFieldId);
        };

        methodInstructionCollector.add(fieldInstruction);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        logger.debug("visitMethodInsn {} {} {} {}", opcode, owner, name, descriptor);
        List<TypeId> argumentTypes = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(type -> AsmUtils.type2TypeId(type))
                .toList();
        TypeId returnType = methodDescriptorToReturnTypeId(descriptor);

        MethodCall methodCall = new MethodCall(TypeId.fromJvmBinaryName(owner), name, argumentTypes, returnType);
        methodInstructionCollector.add(methodCall);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitLdcInsn(Object value) {
        logger.debug("visitLdcInsn {}", value);
        if (value instanceof Type typeValue) {
            // `Xxx.class` などのクラス参照を読み込む
            var typeId = AsmUtils.type2TypeId(typeValue);
            methodInstructionCollector.add(new ClassReference(typeId));
        }

        super.visitLdcInsn(value);
    }

    /**
     * invokeDynamicを処理する。
     * 通常はLambdaやメソッド参照を記述した場合だが、JVM言語を使用すると不意に現れる可能性がある。
     * ここではJava言語でのLambdaやメソッド参照と想定して処理している。
     */
    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object...
            bootstrapMethodArguments) {
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
                var handleOwnerType = TypeId.fromJvmBinaryName(handle.getOwner());
                var handleMethodName = handle.getName();
                var handleArgumentTypes = Arrays.stream(Type.getArgumentTypes(handle.getDesc()))
                        .map(type1 -> AsmUtils.type2TypeId(type1))
                        .toList();
                var handleReturnType = methodDescriptorToReturnTypeId(handle.getDesc());
                var handleMethodCall = new MethodCall(handleOwnerType, handleMethodName, handleArgumentTypes, handleReturnType);

                // returnType/argumentTypesは呼び出し側としての型。同じになることも多いが、違うこともある。
                // たとえば　int method() をメソッド参照で呼び出すと ()Ljava/lang/Integer; になったりする。
                var returnType = AsmUtils.type2TypeId(type.getReturnType());
                var argumentTypes = Arrays.stream(type.getArgumentTypes()).map(type1 -> AsmUtils.type2TypeId(type1)).toList();
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
        List<String> caseTargets = Arrays.stream(labels).map(Label::toString).toList();
        methodInstructionCollector.add(SwitchInstruction.lookup(dflt.toString(), caseTargets));
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        logger.debug("visitTableSwitchInsn {} {} {} {}", min, max, dflt, labels);
        List<String> caseTargets = Arrays.stream(labels).map(Label::toString).toList();
        methodInstructionCollector.add(SwitchInstruction.table(dflt.toString(), caseTargets));
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        logger.debug("visitJumpInsn {} {}", opcode, label);
        // if<cond> はJumpInsnにくるのでこのメソッドで判定があるかを検出するが、
        // GOTOやJSR（Java7で削除されたJump to Subroutine。ASMに存在するので一応。）は
        // 判定せずの移動だけなので、「判定」の記録からは除外する。
        if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR) {
            var kind = switch (opcode) {
                case Opcodes.IFEQ, Opcodes.IFNE,
                     Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE,
                     Opcodes.IFLT, Opcodes.IFLE, Opcodes.IFGE, Opcodes.IFGT,
                     Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE,
                     Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE, Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE ->
                        IfInstruction.Kind.比較;
                case Opcodes.IFNULL, Opcodes.IFNONNULL -> IfInstruction.Kind.NULL判定;
                default -> {
                    // ここには来ないはずだが、来た場合に続行不能にしないためにログ出力しておく
                    logger.warn("unknown opcode {} in visitJumpInsn.", opcode);
                    yield IfInstruction.Kind.UNKNOWN;
                }
            };

            methodInstructionCollector.add(IfInstruction.from(kind, label.toString()));
        }
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        methodInstructionCollector.add(new TryCatchInstruction(
                new JumpTarget(start.toString()),
                new JumpTarget(end.toString()),
                new JumpTarget(handler.toString()),
                type
        ));
        super.visitTryCatchBlock(start, end, handler, type);
    }

    /**
     * JumpやSwitchのジャンプ先になるLabel
     */
    @Override
    public void visitLabel(Label label) {
        methodInstructionCollector.add(new JumpTarget(label.toString()));
        super.visitLabel(label);
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

    private static TypeId methodDescriptorToReturnTypeId(String descriptor) {
        return AsmUtils.type2TypeId(Type.getReturnType(descriptor));
    }
}
