package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.instruction.DynamicMethodCall;
import org.dddjava.jig.domain.model.data.members.instruction.Instruction;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.LambdaExpressionCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * クラスのバイトコードから必要な情報を抽出するClassVisitorの実装
 *
 * ```
 * visit
 * [ visitSource ]
 * [ visitModule ]
 * [ visitNestHost ]
 * [ visitOuterClass ]
 * ( visitAnnotation | visitTypeAnnotation | visitAttribute )*
 * (
 * visitNestMember
 * | [ * visitPermittedSubclass ]
 * | visitInnerClass
 * | visitRecordComponent
 * | visitField
 * | visitMethod
 * )*
 * visitEnd
 * ```
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html">JVMS/Chapter 4. The class File Format</a>
 */
class AsmClassVisitor extends ClassVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmClassVisitor.class);

    @Nullable
    private TypeId typeId;

    @Nullable
    private JigTypeHeaderBuilder jigTypeHeaderBuilder;
    @Nullable
    private JigTypeHeader jigTypeHeader;

    private final ArrayList<JigAnnotationReference> declarationAnnotationCollector = new ArrayList<>();
    private boolean isStaticNestedClass = false;

    public boolean isEnum() {
        return Objects.requireNonNull(jigTypeHeaderBuilder)
                .baseTypeDataBundle()
                .superType()
                .filter(superType -> superType.typeIs(Enum.class))
                .isPresent();
    }

    public boolean isRecord() {
        return Objects.requireNonNull(jigTypeHeaderBuilder)
                .baseTypeDataBundle()
                .superType()
                .filter(superType -> superType.typeIs(Record.class))
                .isPresent();
    }

    // FieldやMethodで使用するもの
    record Pair<T1, T2>(T1 header, T2 body) {
    }

    private final Collection<JigFieldHeader> fieldHeaders = new ArrayList<>();
    private final Collection<Pair<JigMethodHeader, List<Instruction>>> methodCollector = new ArrayList<>();
    private final Set<String> recordComponentNames = new HashSet<>();

    AsmClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String classInternalName, @Nullable String signature, String superName, String[] interfaces) {
        var typeId = this.typeId = TypeId.fromJvmBinaryName(classInternalName);
        var jigTypeModifiers = resolveTypeModifiers(access);
        var jigTypeKind = resolveTypeKind(access);
        var jigTypeVisibility = resolveVisibility(access);

        if (signature != null) {
            AsmClassSignatureVisitor asmClassSignatureVisitor = new AsmClassSignatureVisitor(api);
            new SignatureReader(signature).accept(asmClassSignatureVisitor);
            this.jigTypeHeaderBuilder = jigTypeHeader(typeId, jigTypeKind, jigTypeVisibility, jigTypeModifiers, asmClassSignatureVisitor.jigTypeParameters(), asmClassSignatureVisitor.jigBaseTypeDataBundle());
        } else {
            // 非総称型で作成
            this.jigTypeHeaderBuilder = jigTypeHeader(typeId, jigTypeKind, jigTypeVisibility, jigTypeModifiers, List.of(),
                    new JigBaseTypeDataBundle(
                            Optional.of(JigTypeReference.fromJvmBinaryName(superName)),
                            Arrays.stream(interfaces).map(JigTypeReference::fromJvmBinaryName).toList()
                    ));
        }
        super.visit(version, access, classInternalName, signature, superName, interfaces);
    }

    private JigTypeHeaderBuilder jigTypeHeader(TypeId typeId, JavaTypeDeclarationKind javaTypeDeclarationKind, JigTypeVisibility jigTypeVisibility, Collection<JigTypeModifier> jigTypeModifiers, List<JigTypeParameter> jigTypeParameters, JigBaseTypeDataBundle jigBaseTypeDataBundle) {
        return new JigTypeHeaderBuilder(typeId, javaTypeDeclarationKind, jigBaseTypeDataBundle, jigTypeVisibility, jigTypeModifiers, jigTypeParameters);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return AsmAnnotationVisitor.from(this.api, descriptor, it -> {
            declarationAnnotationCollector.add(it.annotationReference());
        });
    }

    /**
     * InnerClassesにはインナークラス/ネストクラスの情報が入っている。
     * 自身がどちらかの場合は自身の情報もはいっており、ネストした場合のみの修飾子はここにあらわれる。
     */
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        // nameが一致するもののみ、このクラスの情報として採用する
        if (TypeId.fromJvmBinaryName(name).equals(this.typeId)) {
            if ((access & Opcodes.ACC_STATIC) != 0) {
                isStaticNestedClass = true;
            }
        }
        super.visitInnerClass(name, outerName, innerName, access);
    }

    /**
     * {@link ClassReader} の読み取り順が recordComponent -> field -> method となっているので、
     * ここで recordComponent の名前を記録して field/method の判定に使える。
     */
    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        recordComponentNames.add(name);
        // TODO record componentのアノテーションを見る必要がある
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, @Nullable String signature, @Nullable Object value) {
        return AsmFieldVisitor.from(this, access, name, descriptor, signature);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, @Nullable String signature, @Nullable String[] exceptions) {
        logger.debug("visitMethod: {}, {}, {}, {}, {}", access, name, descriptor, signature, exceptions);
        return AsmMethodVisitor.from(this, access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        // この時点で空はないけどNullableなのでNullでないことを宣言
        Objects.requireNonNull(jigTypeHeaderBuilder);

        EnumSet<JigTypeModifier> jigTypeModifiers = EnumSet.noneOf(JigTypeModifier.class);
        jigTypeModifiers.addAll(jigTypeHeaderBuilder.jigTypeModifiers());
        // staticなネストクラスの場合の修飾子を追加。JVMSではフラグはないが、JLSでは修飾子を記述するので、ここで追加する。
        if (isStaticNestedClass) {
            jigTypeModifiers.add(JigTypeModifier.STATIC);
        }

        // 情報が揃ったのでjigTypeHeaderを構築する
        jigTypeHeader = new JigTypeHeader(jigTypeHeaderBuilder.id(), jigTypeHeaderBuilder.javaTypeDeclarationKind(),
                new JigTypeAttributes(
                        jigTypeHeaderBuilder.jigTypeVisibility(),
                        jigTypeModifiers,
                        List.copyOf(declarationAnnotationCollector),
                        jigTypeHeaderBuilder.typeParameters()
                ),
                jigTypeHeaderBuilder.baseTypeDataBundle()
        );

        super.visitEnd();
    }

    /**
     * Visibilityに持っていきたいが、accessの定数はasmが持っているのでここに置いておく。
     * 実際はバイトコードの固定値。
     *
     * classの場合、ソースコードではpublic,protected,default,privateは定義できるが、
     * バイトコードではpublicか否かしか識別できない。
     * さらにprotectedもpublicになる。（パッケージ外から参照可能なので。）
     *
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.1">The ClassFile Structure</a> のaccess_flag
     */
    private JigTypeVisibility resolveVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return JigTypeVisibility.PUBLIC;
        return JigTypeVisibility.NOT_PUBLIC;
    }

    /**
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.1">The ClassFile Structure</a> のaccess_flag
     */
    private Collection<JigTypeModifier> resolveTypeModifiers(int access) {
        EnumSet<JigTypeModifier> set = EnumSet.noneOf(JigTypeModifier.class);
        if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            set.add(JigTypeModifier.ABSTRACT);
        }
        if ((access & Opcodes.ACC_FINAL) != 0) {
            set.add(JigTypeModifier.FINAL);
        }
        return set;
    }

    /**
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.1">The ClassFile Structure</a> のaccess_flag
     */
    private JavaTypeDeclarationKind resolveTypeKind(int access) {
        if ((access & Opcodes.ACC_ENUM) != 0) return JavaTypeDeclarationKind.ENUM;
        if ((access & Opcodes.ACC_INTERFACE) != 0) return JavaTypeDeclarationKind.INTERFACE;
        if ((access & Opcodes.ACC_ANNOTATION) != 0) return JavaTypeDeclarationKind.ANNOTATION;
        // ASM独自
        if ((access & Opcodes.ACC_RECORD) != 0) return JavaTypeDeclarationKind.RECORD;
        // 不明なものはCLASSにしておく
        return JavaTypeDeclarationKind.CLASS;
    }

    public TypeId typeId() {
        // visitの先頭で入るのでNullなことはほぼない
        return Objects.requireNonNull(typeId);
    }

    ClassDeclaration classDeclaration() {
        // lambda合成メソッドを名前でひけるように収集
        Map<String, List<Instruction>> lambdaMethodMap = methodCollector.stream()
                .filter(collectedMethod ->
                        collectedMethod.header().isLambdaSyntheticMethod())
                .collect(toMap(it -> it.header().name(), it -> it.body()));

        // method内でlambda式を実装している場合にLambda合成メソッドのInstructionを関連づける
        Collection<JigMethodDeclaration> methodDeclarations = methodCollector.stream()
                .map(it -> {
                    List<Instruction> instructions = it.body().stream()
                            .map(instruction -> resolveInstruction(instruction, lambdaMethodMap))
                            .toList();
                    return new JigMethodDeclaration(it.header(), new Instructions(instructions));
                })
                .toList();

        // jigTypeHeaderはvisitEndで入るので、visitEnd後しかこのメソッドは呼んではいけない
        return new ClassDeclaration(Objects.requireNonNull(jigTypeHeader), fieldHeaders, methodDeclarations);
    }

    private static Instruction resolveInstruction(Instruction instruction, Map<String, List<Instruction>> lambdaMethodMap) {
        // dynamicMethodCallの呼び出しメソッドと合致するものがあればLambdaExpressionCallにラップする
        if (instruction instanceof DynamicMethodCall dynamicMethodCall) {
            String name = dynamicMethodCall.methodCall().methodName();
            if (lambdaMethodMap.containsKey(name)) {
                List<Instruction> instructionList = lambdaMethodMap.get(name).stream()
                        // lambdaのネストに対応するために再帰
                        .map(it -> resolveInstruction(it, lambdaMethodMap)).toList();
                return LambdaExpressionCall.from(dynamicMethodCall, new Instructions(instructionList));
            }
        }
        // 置き換えないものは何もしない
        return instruction;
    }

    int api() {
        return api;
    }

    boolean isRecordComponentName(String name) {
        // recordであることと引数0の確認後なので名前比較だけでOK
        return recordComponentNames.contains(name);
    }

    void addJigFieldHeader(JigFieldHeader jigFieldHeader) {
        fieldHeaders.add(jigFieldHeader);
    }

    public void finishVisitMethod(JigMethodHeader jigMethodHeader, List<Instruction> methodInstructionList) {
        // lambda式の展開のためにこの形で保持しておく
        methodCollector.add(new Pair<>(jigMethodHeader, methodInstructionList));
    }
}
