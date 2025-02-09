package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.sources.classsources.ClassDeclaration;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
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
 */
class AsmClassVisitor extends ClassVisitor {
    static Logger logger = LoggerFactory.getLogger(AsmClassVisitor.class);

    private final JigMemberBuilder jigMemberBuilder = new JigMemberBuilder();

    private TypeIdentifier typeIdentifier;

    private JigTypeHeader jigTypeHeader;
    private ArrayList<JigAnnotationReference> declarationAnnotationCollector = new ArrayList<>();
    private boolean isStaticNestedClass = false;

    AsmClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String classInternalName, String signature, String superName, String[] interfaces) {
        this.typeIdentifier = TypeIdentifier.fromJvmBinaryName(classInternalName);
        var jigTypeModifiers = resolveTypeModifiers(access);
        var jigTypeKind = resolveTypeKind(access);
        var jigTypeVisibility = resolveVisibility(access);

        if (signature != null) {
            AsmClassSignatureVisitor asmClassSignatureVisitor = new AsmClassSignatureVisitor(api);
            new SignatureReader(signature).accept(asmClassSignatureVisitor);
            jigTypeHeader = jigTypeHeader(jigTypeKind, jigTypeVisibility, jigTypeModifiers, asmClassSignatureVisitor.jigTypeParameters(), asmClassSignatureVisitor.jigBaseTypeDataBundle());
        } else {
            // 非総称型で作成
            jigTypeHeader = jigTypeHeader(jigTypeKind, jigTypeVisibility, jigTypeModifiers, List.of(),
                    new JigBaseTypeDataBundle(
                            Optional.of(JigTypeReference.fromJvmBinaryName(superName)),
                            Arrays.stream(interfaces).map(JigTypeReference::fromJvmBinaryName).toList()
                    ));
        }
        super.visit(version, access, classInternalName, signature, superName, interfaces);
    }

    private JigTypeHeader jigTypeHeader(JigTypeKind jigTypeKind, JigTypeVisibility jigTypeVisibility, Collection<JigTypeModifier> jigTypeModifiers, List<JigTypeParameter> jigTypeParameters, JigBaseTypeDataBundle jigBaseTypeDataBundle) {
        // アノテーションはまだ取得していないので空で作る
        return new JigTypeHeader(this.typeIdentifier, jigTypeKind, new JigTypeAttributeData(jigTypeVisibility, jigTypeModifiers, Collections.emptyList(), jigTypeParameters), jigBaseTypeDataBundle);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AsmAnnotationVisitor(this.api, AsmUtils.typeDescriptorToIdentifier(descriptor), it -> {
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
        if (TypeIdentifier.fromJvmBinaryName(name).equals(this.typeIdentifier)) {
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
        jigMemberBuilder.addRecordComponent(name, AsmUtils.typeDescriptorToIdentifier(descriptor));
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return AsmFieldVisitor.from(this.api, access, name, descriptor, signature, this.typeIdentifier, this.jigMemberBuilder);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        boolean isEnum = jigTypeHeader.jigTypeKind() == JigTypeKind.ENUM;
        return AsmMethodVisitor.from(this.api, access, name, descriptor, signature, exceptions, typeIdentifier, isEnum, jigMemberBuilder);
    }

    @Override
    public void visitEnd() {
        // 変更要因があったら作り直す
        if (isStaticNestedClass || !declarationAnnotationCollector.isEmpty()) {
            EnumSet<JigTypeModifier> jigTypeModifiers = EnumSet.noneOf(JigTypeModifier.class);
            jigTypeModifiers.addAll(jigTypeHeader.jigTypeAttributeData().jigTypeModifiers());
            if (isStaticNestedClass) {
                jigTypeModifiers.add(JigTypeModifier.STATIC);
            }
            jigTypeHeader = new JigTypeHeader(jigTypeHeader.id(), jigTypeHeader.jigTypeKind(),
                    new JigTypeAttributeData(
                            jigTypeHeader.jigTypeAttributeData().jigTypeVisibility(),
                            jigTypeModifiers,
                            List.copyOf(declarationAnnotationCollector),
                            jigTypeHeader.jigTypeAttributeData().typeParameters()
                    ),
                    jigTypeHeader.baseTypeDataBundle()
            );
        }
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
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.1">The ClassFile Structure</a> のaccess_flag
     */
    private JigTypeVisibility resolveVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return JigTypeVisibility.PUBLIC;
        return JigTypeVisibility.NOT_PUBLIC;
    }

    /**
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.1">The ClassFile Structure</a> のaccess_flag
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
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.1">The ClassFile Structure</a> のaccess_flag
     */
    private JigTypeKind resolveTypeKind(int access) {
        if ((access & Opcodes.ACC_ENUM) != 0) return JigTypeKind.ENUM;
        if ((access & Opcodes.ACC_INTERFACE) != 0) return JigTypeKind.INTERFACE;
        if ((access & Opcodes.ACC_ANNOTATION) != 0) return JigTypeKind.ANNOTATION;
        // ASM独自
        if ((access & Opcodes.ACC_RECORD) != 0) return JigTypeKind.RECORD;
        // 不明なものはCLASSにしておく
        return JigTypeKind.CLASS;
    }

    public JigMemberBuilder jigMemberBuilder() {
        // visitEnd後にしか呼んではいけない
        return Objects.requireNonNull(jigMemberBuilder);
    }

    public JigTypeHeader jigTypeHeader() {
        return jigTypeHeader;
    }

    ClassDeclaration classDeclaration() {
        return new ClassDeclaration(jigMemberBuilder(), jigTypeHeader());
    }
}
