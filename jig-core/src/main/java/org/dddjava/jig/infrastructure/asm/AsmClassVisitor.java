package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldType;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.sources.JigMethodBuilder;
import org.dddjava.jig.domain.model.sources.JigTypeBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
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

    private final JigTypeBuilder jigTypeBuilder = new JigTypeBuilder();

    private TypeIdentifier typeIdentifier;

    private JigTypeHeader jigTypeHeader;
    private boolean isStaticNestedClass = false;

    AsmClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String classInternalName, String signature, String superName, String[] interfaces) {
        this.typeIdentifier = TypeIdentifier.fromJvmBinaryName(classInternalName);

        // access: https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.1-200-E.1
        var jigTypeModifiers = resolveTypeModifiers(access);
        var jigTypeKind = resolveTypeKind(access);
        var jigTypeVisibility = resolveVisibility(access);

        // ジェネリクスを使用している場合だけsignatureが入る
        if (signature != null) {
            logger.debug(signature);
            AsmClassSignatureVisitor asmClassSignatureVisitor = new AsmClassSignatureVisitor(api);
            new SignatureReader(signature).accept(asmClassSignatureVisitor);

            jigTypeHeader = new JigTypeHeader(
                    this.typeIdentifier,
                    jigTypeKind,
                    new JigTypeAttributeData(
                            jigTypeVisibility,
                            jigTypeModifiers,
                            new ArrayList<>(), // アノテーションは後で追加する
                            asmClassSignatureVisitor.jigTypeParameters()
                    ),
                    asmClassSignatureVisitor.jigBaseTypeDataBundle()
            );
        } else {
            // 非総称型で作成
            jigTypeHeader = new JigTypeHeader(
                    this.typeIdentifier,
                    jigTypeKind,
                    new JigTypeAttributeData(
                            jigTypeVisibility,
                            jigTypeModifiers,
                            new ArrayList<>(), // アノテーションは後で追加する
                            List.of()
                    ),
                    new JigBaseTypeDataBundle(
                            Optional.of(JigBaseTypeData.fromJvmBinaryName(superName)),
                            Arrays.stream(interfaces).map(JigBaseTypeData::fromJvmBinaryName).toList()
                    )
            );
        }

        super.visit(version, access, classInternalName, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AsmAnnotationVisitor(this.api, typeDescriptorToIdentifier(descriptor), annotation -> {
            jigTypeBuilder.addAnnotation(annotation);
            jigTypeHeader.jigTypeAttributeData().declarationAnnotationInstances().add(JigAnnotationInstance.from(annotation.typeIdentifier()));
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
        jigTypeBuilder.addRecordComponent(name, typeDescriptorToIdentifier(descriptor));
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールド
            FieldType result;
            if (signature == null) {
                TypeIdentifier typeIdentifier = typeDescriptorToIdentifier(descriptor);
                result = new FieldType(typeIdentifier);
            } else {
                ArrayList<TypeIdentifier> typeParameters = new ArrayList<>();
                new SignatureReader(signature).accept(
                        new SignatureVisitor(AsmClassVisitor.this.api) {
                            @Override
                            public SignatureVisitor visitTypeArgument(char wildcard) {
                                if (wildcard == '=') {
                                    return new SignatureVisitor(this.api) {
                                        @Override
                                        public void visitClassType(String name1) {
                                            typeParameters.add(TypeIdentifier.valueOf(name1));
                                        }
                                    };
                                }
                                return super.visitTypeArgument(wildcard);
                            }
                        }
                );
                TypeIdentifiers typeIdentifiers = new TypeIdentifiers(typeParameters);
                result = new FieldType(typeDescriptorToIdentifier(descriptor), typeIdentifiers);
            }

            FieldType fieldType = result;

            return new AsmFieldVisitor(this.api, it -> {
                FieldDeclaration fieldDeclaration = jigTypeBuilder.addInstanceField(typeIdentifier, fieldType, name);
                it.annotations.forEach(annotation -> {
                    jigTypeBuilder.addFieldAnnotation(new FieldAnnotation(annotation, fieldDeclaration));
                });
            });
        } else if (!name.equals("$VALUES")) {
            // staticフィールドのうち、enumにコンパイル時に作成される $VALUES は除く
            jigTypeBuilder.addStaticField(typeIdentifier, typeDescriptorToIdentifier(descriptor), name);
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return AsmMethodVisitor.from(this.api,
                access, name, descriptor, signature, exceptions,
                typeIdentifier,
                data -> {
                    JigMethodBuilder jigMethodBuilder = JigMethodBuilder.builder(
                            access,
                            data.visibility,
                            data.signatureContainedTypes,
                            data.throwsTypes,
                            data.methodDeclaration,
                            data.annotationList,
                            data.methodInstructions,
                            jigTypeHeader.jigTypeKind() == JigTypeKind.ENUM,
                            jigTypeBuilder.isRecordComponent(data.methodDeclaration));

                    if (jigMethodBuilder.methodIdentifier().methodSignature().isConstructor()) {
                        // コンストラクタ
                        jigTypeBuilder.addConstructor(jigMethodBuilder);
                    } else if ((access & Opcodes.ACC_STATIC) != 0) {
                        // staticメソッド
                        jigTypeBuilder.addStaticMethod(jigMethodBuilder);
                    } else {
                        // コンストラクタでもstaticメソッドでもない＝インスタンスメソッド
                        jigTypeBuilder.addInstanceMethod(jigMethodBuilder);
                    }
                });
    }

    @Override
    public void visitEnd() {

        if (isStaticNestedClass) {
            jigTypeHeader = jigTypeHeader.withStatic();
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

    static TypeIdentifier typeDescriptorToIdentifier(String descriptor) {
        Type type = Type.getType(descriptor);
        return TypeIdentifier.valueOf(type.getClassName());
    }

    public JigTypeBuilder jigTypeBuilder() {
        // visitEnd後にしか呼んではいけない
        return Objects.requireNonNull(jigTypeBuilder);
    }

    public JigTypeHeader jigTypeHeader() {
        return jigTypeHeader;
    }
}
